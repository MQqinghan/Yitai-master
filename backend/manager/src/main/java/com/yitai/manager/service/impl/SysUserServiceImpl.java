package com.yitai.manager.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.yitai.common.exception.YitaiException;
import com.yitai.common.log.annotation.Log;
import com.yitai.manager.mapper.SysRoleUserMapper;
import com.yitai.manager.mapper.SysUserMapper;
import com.yitai.manager.service.SysUserService;
import com.yitai.model.dto.system.AssginRoleDto;
import com.yitai.model.dto.system.LoginDto;
import com.yitai.model.dto.system.SysUserDto;
import com.yitai.model.entity.system.SysUser;
import com.yitai.model.vo.common.ResultCodeEnum;
import com.yitai.model.vo.system.LoginVo;
import com.yitai.model.vo.system.ValidateCodeVo; // 新增：验证码返回VO
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleUserMapper sysRoleUserMapper;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    // ========== 新增：生成验证码方法 ==========
    @Override
    public ValidateCodeVo generateValidateCode() {
        // 1. 生成4位随机验证码
        String code = RandomUtil.randomString(4);
        System.out.println("生成的验证码：" + code); // 测试用，生产环境可删除

        // 2. 生成验证码图片（简单版，如需复杂样式可调整）
        BufferedImage image = createValidateCodeImage(code);

        // 3. 将图片转为Base64字符串（前端可直接显示）
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "PNG", bos);
            byte[] imageBytes = bos.toByteArray();
            String imageBase64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);

            // 4. 生成验证码唯一标识（codeKey），存入Redis（5分钟过期）
            String codeKey = UUID.randomUUID().toString().replaceAll("-", "");
            redisTemplate.opsForValue().set("validate:code:" + codeKey, code, 5, TimeUnit.MINUTES);

            // 5. 封装返回结果
            ValidateCodeVo validateCodeVo = new ValidateCodeVo();
            validateCodeVo.setCodeKey(codeKey);
            validateCodeVo.setCodeValue(imageBase64);
            return validateCodeVo;
        } catch (Exception e) {
            e.printStackTrace();
            throw new YitaiException(ResultCodeEnum.SYSTEM_ERROR);
        }
    }

    // 辅助方法：生成验证码图片
    private BufferedImage createValidateCodeImage(String code) {
        int width = 120;
        int height = 48;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();

        // 1. 填充背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 2. 绘制边框
        g.setColor(Color.GRAY);
        g.drawRect(0, 0, width - 1, height - 1);

        // 3. 绘制干扰线
        RandomUtil.randomInt(10);
        for (int i = 0; i < 10; i++) {
            g.setColor(new Color(RandomUtil.randomInt(256), RandomUtil.randomInt(256), RandomUtil.randomInt(256)));
            g.drawLine(RandomUtil.randomInt(width), RandomUtil.randomInt(height),
                    RandomUtil.randomInt(width), RandomUtil.randomInt(height));
        }

        // 4. 绘制验证码文字
        g.setColor(new Color(20, 100, 200));
        g.setFont(new Font("Arial", Font.BOLD, 28));
        for (int i = 0; i < code.length(); i++) {
            g.drawString(String.valueOf(code.charAt(i)), 20 + i * 25, 35);
        }

        g.dispose();
        return image;
    }

    // ========== 修改：登录方法（添加验证码校验） ==========
    @Override
    public LoginVo login(LoginDto loginDto) {
        // ========== 新增：验证码校验（仅加这部分，复用已有枚举和参数） ==========
        String captcha = loginDto.getCaptcha(); // 前端传入的验证码
        String codeKey = loginDto.getCodeKey(); // 验证码唯一标识
        System.out.println("===== 验证码校验 =====");
        System.out.println("前端传入的验证码: " + captcha);
        System.out.println("验证码Key: " + codeKey);

        // 1. 校验参数是否为空
        if (StrUtil.isEmpty(captcha) || StrUtil.isEmpty(codeKey)) {
            System.out.println("错误原因：验证码或CodeKey为空");
            // 直接用已有枚举 VALIDATECODE_ERROR
            throw new YitaiException(ResultCodeEnum.VALIDATECODE_ERROR);
        }

        // 2. 从Redis获取真实验证码（key格式要和ValidateCodeService中一致）
        String redisCode = redisTemplate.opsForValue().get("user:validate" + codeKey);
        System.out.println("从Redis读取的真实验证码: " + redisCode); // 新增日志，验证是否读取成功
        if (StrUtil.isEmpty(redisCode)) {
            System.out.println("错误原因：验证码已过期或不存在");
            throw new YitaiException(ResultCodeEnum.VALIDATECODE_ERROR);
        }

        // 3. 比较验证码（忽略大小写，提升用户体验）
        if (!redisCode.equalsIgnoreCase(captcha)) {
            System.out.println("错误原因：验证码输入错误，正确值：" + redisCode);
            throw new YitaiException(ResultCodeEnum.VALIDATECODE_ERROR);
        }

        // 4. 校验通过后删除Redis中的验证码（防止重复使用）
        redisTemplate.delete("user:validate" + codeKey);

        // ========== 原有用户名密码校验逻辑（完全不变） ==========
        String userName = loginDto.getUserName();
        String inputPassword = loginDto.getPassword();
        System.out.println("===== 登录校验日志开始 =====");
        System.out.println("前端传入的用户名: " + userName);
        System.out.println("前端传入的密码: " + inputPassword);

        SysUser sysUser = sysUserMapper.selectUserInfoByUserName(userName);
        System.out.println("查询到的用户对象: " + sysUser);
        if(sysUser == null) {
            System.out.println("错误原因：根据用户名" + userName + "未查询到用户");
            System.out.println("===== 登录校验日志结束 =====");
            throw new YitaiException(ResultCodeEnum.LOGIN_ERROR);
        }

        String databasePassword = sysUser.getPassword();
        String salt = sysUser.getSalt();
        System.out.println("数据库存储的密码: " + databasePassword);
        System.out.println("数据库存储的盐值: " + salt);

        String inputPasswordWithSalt = inputPassword + salt;
        System.out.println("密码+盐值拼接结果: " + inputPasswordWithSalt);
        String encryptInputPassword = DigestUtils.md5DigestAsHex(inputPasswordWithSalt.getBytes(StandardCharsets.UTF_8));
        System.out.println("加密后的密码: " + encryptInputPassword);
        System.out.println("加密密码与数据库密码是否匹配: " + encryptInputPassword.equals(databasePassword));

        if(!encryptInputPassword.equals(databasePassword)) {
            System.out.println("错误原因：密码加密后与数据库密码不匹配");
            System.out.println("===== 登录校验日志结束 =====");
            throw new YitaiException(ResultCodeEnum.LOGIN_ERROR);
        }

        // 生成token、存入Redis（不变）
        String token = UUID.randomUUID().toString().replaceAll("-","");
        System.out.println("生成的登录token: " + token);

        redisTemplate.opsForValue()
                .set("user:login"+token,
                        JSON.toJSONString(sysUser),
                        7,
                        TimeUnit.DAYS);
        System.out.println("用户信息已存入Redis，key: user:login" + token);
        System.out.println("===== 登录校验日志结束 =====");

        LoginVo loginVo = new LoginVo();
        loginVo.setToken(token);
        return loginVo;
    }

    // 以下原有方法（getUserInfo、logout、findByPage等）保持不变，无需修改
    @Override
    public SysUser getUserInfo(String token) {
        String userJson = redisTemplate.opsForValue().get("user:login" + token);
        SysUser sysUser = JSON.parseObject(userJson, SysUser.class);
        return sysUser;
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete("user:login" + token);
    }

    @Override
    public PageInfo<SysUser> findByPage(Integer pageNum, Integer pageSize, SysUserDto sysUserDto) {
        PageHelper.startPage(pageNum,pageSize);
        List<SysUser> list = sysUserMapper.findByPage(sysUserDto);
        PageInfo<SysUser>  pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public void saveSysUser(SysUser sysUser) {
        String userName = sysUser.getUserName();
        SysUser dbSysUser = sysUserMapper.selectUserInfoByUserName(userName);
        if(dbSysUser != null) {
            throw new YitaiException(ResultCodeEnum.USER_NAME_IS_EXISTS);
        }

        String salt = RandomUtil.randomString(8);
        sysUser.setSalt(salt);

        String passwordWithSalt = sysUser.getPassword() + salt;
        String encryptPassword = DigestUtils.md5DigestAsHex(passwordWithSalt.getBytes());
        sysUser.setPassword(encryptPassword);

        sysUser.setStatus(1);
        sysUserMapper.save(sysUser);
    }

    @Override
    public void updateSysUser(SysUser sysUser) {
        if(StrUtil.isNotEmpty(sysUser.getPassword())) {
            String salt = RandomUtil.randomString(8);
            sysUser.setSalt(salt);
            String passwordWithSalt = sysUser.getPassword() + salt;
            String encryptPassword = DigestUtils.md5DigestAsHex(passwordWithSalt.getBytes());
            sysUser.setPassword(encryptPassword);
        }
        sysUserMapper.update(sysUser);
    }

    @Override
    public void deleteById(Long userId) {
        sysUserMapper.delete(userId);
    }

    @Log(title = "用户分配角色",businessType = 0)
    @Transactional
    @Override
    public void doAssign(AssginRoleDto assginRoleDto) {
        sysRoleUserMapper.deleteByUserId(assginRoleDto.getUserId());
        List<Long> roleIdList = assginRoleDto.getRoleIdList();
        for(Long roleId:roleIdList) {
            sysRoleUserMapper.doAssign(assginRoleDto.getUserId(),roleId);
        }
    }
}
