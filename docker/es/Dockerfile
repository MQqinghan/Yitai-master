# ES 7.17.x + IK 中文分词（与 spring-boot 3.0.5 管理的 rest-high-level-client 7.17.x 匹配）
FROM elasticsearch:7.17.26
# IK 插件（infini.cloud 为 medcl/github release 的镜像源；若网络不通可换：
# https://github.com/infinilabs/analysis-ik/releases/download/v7.17.26/elasticsearch-analysis-ik-7.17.26.zip）
RUN elasticsearch-plugin install --batch https://get.infini.cloud/elasticsearch/analysis-ik/7.17.26
