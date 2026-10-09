<template>
  <div class="ai-fab">
    <div v-if="open" class="ai-panel">
      <div class="ai-header">
        <div class="ai-tabs">
          <span :class="{ active: mode === 'shopping' }" @click="mode = 'shopping'">智能导购</span>
          <span :class="{ active: mode === 'service' }" @click="mode = 'service'">智能客服</span>
        </div>
        <div class="ai-header-right">
          <button class="ai-clear" @click="clearChat" title="清空对话（同时删除上下文）">清空</button>
          <button class="ai-close" @click="open = false">×</button>
        </div>
      </div>

      <div class="ai-messages" ref="box">
        <div v-if="activeMessages.length === 0" class="ai-empty">
          {{ mode === 'shopping' ? '我是导购小易，告诉我想买什么，我帮你找～' : '我是客服小易，退换货/物流/支付问题问我吧～' }}
        </div>
        <div v-for="(m, i) in activeMessages" :key="i" :class="['ai-msg', m.role]">
          <div class="ai-bubble" v-html="m.content ? rich(m.content) : (m.pending ? '正在为您挑选…' : '…')"></div>
          <div v-if="showDebug && m.tools && m.tools.length" class="ai-tools">
            <span v-for="(t, ti) in m.tools" :key="ti" class="ai-tool">
              🔧 {{ t.tool }}<template v-if="t.args">（{{ fmtArgs(t.args) }}）</template>
            </span>
          </div>
          <div v-if="m.products && m.products.length" class="ai-products">
            <div v-for="(p, pi) in m.products" :key="pi" class="ai-product" @click="openDetail(p.skuId)">
              <div class="ai-product-thumb">{{ (p.name || '商品').slice(0, 1) }}</div>
              <div class="ai-product-info">
                <div class="ai-product-name">{{ p.name || ('SKU ' + p.skuId) }}</div>
                <div class="ai-product-price">¥{{ p.price }}</div>
              </div>
              <div class="ai-product-go">查看 ›</div>
            </div>
          </div>
        </div>
      </div>

      <div class="ai-input">
        <input v-model="input" :disabled="streaming" @keyup.enter="send" placeholder="问我点什么吧～" />
        <button @click="send" :disabled="streaming">发送</button>
      </div>
      <div class="ai-hint">演示模式基于真实商品数据作答；配置 API Key 后为真实大模型对话</div>
    </div>

    <button v-else class="ai-bubble-btn" @click="open = true">🤖 AI</button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch, onMounted } from 'vue'
import { chatStream, deleteHistory } from '@/api/ai'
import { useGoDetail } from '@/hooks/useGoDetail'

interface Msg {
  role: 'user' | 'bot'
  content: string
  tools: Array<{ tool: string; args?: Record<string, unknown> }>
  products?: Array<{ skuId: number | string; name?: string; price?: number | string }>
  pending?: boolean
}

const STORAGE_KEY = (m: string) => 'ai_hist_' + m

function loadHist(m: string): Msg[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY(m))
    return raw ? (JSON.parse(raw) as Msg[]) : []
  } catch {
    return []
  }
}
function saveHist() {
  localStorage.setItem(STORAGE_KEY('shopping'), JSON.stringify(shoppingMessages.value))
  localStorage.setItem(STORAGE_KEY('service'), JSON.stringify(serviceMessages.value))
}

const { navigateToProductDetail } = useGoDetail()

const open = ref(false)
const mode = ref<'shopping' | 'service'>('shopping')
const input = ref('')
// 工具调用过程仅开发模式可见（npm run dev 显示，npm run build 的正式包自动隐藏）
const showDebug = ref(import.meta.env.DEV)
// 导购 / 客服各自独立的消息列表，切换 Tab 互不干扰（解决“上下文串着”）
const shoppingMessages = ref<Msg[]>(loadHist('shopping'))
const serviceMessages = ref<Msg[]>(loadHist('service'))
const streaming = ref(false)
const box = ref<HTMLElement | null>(null)

// 每个模式各自维护独立的 conversationId，后端据此隔离多轮历史
const convIds = ref<{ shopping: string; service: string }>({ shopping: '', service: '' })
const activeMessages = computed(() =>
  mode.value === 'shopping' ? shoppingMessages.value : serviceMessages.value
)

function ensureConvId(): string {
  const m = mode.value
  if (!convIds.value[m]) {
    const uid =
      typeof crypto !== 'undefined' && crypto.randomUUID
        ? crypto.randomUUID()
        : String(Date.now()) + Math.random()
    convIds.value[m] = 'conv-' + m + '-' + uid
  }
  return convIds.value[m]
}

function openDetail(skuId: number | string) {
  navigateToProductDetail(skuId)
}

// 清空当前模式对话：清前端 + 删后端上下文
async function clearChat() {
  const m = mode.value
  const cid = convIds.value[m]
  if (cid) {
    try {
      await deleteHistory(cid)
    } catch {
      // 后端内存删除失败不影响前端体验
    }
  }
  if (m === 'shopping') shoppingMessages.value = []
  else serviceMessages.value = []
  convIds.value[m] = ''
  localStorage.removeItem(STORAGE_KEY(m))
  streaming.value = false
}

function fmtArgs(a?: Record<string, unknown>) {
  if (!a) return ''
  return Object.entries(a)
    .map(([k, v]) => `${k}=${v}`)
    .join(', ')
}

// 轻量 markdown 渲染：转义 HTML 后仅支持 **加粗**，避免 LLM 输出的星号原样露出
function rich(text: string): string {
  const esc = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  return esc.replace(/\*\*([^*\n]+)\*\*/g, '<strong>$1</strong>')
}

function scrollToBottom() {
  nextTick(() => {
    if (box.value) box.value.scrollTop = box.value.scrollHeight
  })
}

watch(activeMessages, scrollToBottom, { deep: true })

function send() {
  const text = input.value.trim()
  if (!text || streaming.value) return
  input.value = ''
  const target = mode.value === 'shopping' ? shoppingMessages : serviceMessages
  target.value.push({ role: 'user', content: text, tools: [] })
  const botMsg: Msg = { role: 'bot', content: '', tools: [], products: [], pending: true }
  target.value.push(botMsg)
  streaming.value = true

  chatStream({ mode: mode.value, message: text, conversationId: ensureConvId() }, {
    onStatus: (t) => {
      botMsg.tools.push(t)
      // 非 dev 模式下也给出温和进度提示，避免用户干等误以为卡死
      if (botMsg.pending && !botMsg.content) {
        botMsg.content = '正在检索商品…'
      }
    },
    onMessage: (piece) => {
      if (botMsg.pending) {
        botMsg.content = ''
        botMsg.pending = false
      }
      botMsg.content += piece
      scrollToBottom()
    },
    onProducts: (cards) => { botMsg.products = cards },
    onError: (e) => {
      botMsg.pending = false
      botMsg.content += '\n[错误] ' + e
      saveHist()
    },
    onDone: () => {
      botMsg.pending = false
      streaming.value = false
      scrollToBottom()
      saveHist()
    }
  }).catch((err) => {
    botMsg.pending = false
    botMsg.content += '\n[连接失败] ' + (err?.message || err)
    streaming.value = false
    saveHist()
  })
}

onMounted(scrollToBottom)
</script>

<style scoped>
.ai-fab {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 9999;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.ai-bubble-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #ff7a45, #ff4d4f);
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  box-shadow: 0 6px 16px rgba(255, 77, 79, 0.4);
}
.ai-bubble-btn:hover {
  transform: scale(1.05);
}
.ai-panel {
  width: 360px;
  height: 520px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.ai-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: linear-gradient(135deg, #ff7a45, #ff4d4f);
  color: #fff;
}
.ai-tabs span {
  margin-right: 14px;
  opacity: 0.7;
  cursor: pointer;
  font-size: 14px;
}
.ai-tabs span.active {
  opacity: 1;
  font-weight: 700;
  border-bottom: 2px solid #fff;
  padding-bottom: 2px;
}
.ai-header-right {
  display: flex;
  align-items: center;
  gap: 4px;
}
.ai-clear {
  background: transparent;
  border: none;
  color: #fff;
  font-size: 12px;
  cursor: pointer;
  opacity: 0.85;
  padding: 2px 6px;
}
.ai-clear:hover {
  opacity: 1;
}
.ai-close {
  background: transparent;
  border: none;
  color: #fff;
  font-size: 22px;
  cursor: pointer;
  line-height: 1;
}
.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  background: #f7f8fa;
}
.ai-empty {
  color: #999;
  font-size: 13px;
  text-align: center;
  margin-top: 40px;
}
.ai-msg {
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
}
.ai-msg.user {
  align-items: flex-end;
}
.ai-msg.bot {
  align-items: flex-start;
}
.ai-bubble {
  max-width: 85%;
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
.ai-bubble :deep(strong) {
  font-weight: 700;
}
.ai-msg.user .ai-bubble {
  background: #ff7a45;
  color: #fff;
}
.ai-msg.bot .ai-bubble {
  background: #fff;
  color: #333;
  border: 1px solid #eee;
}
.ai-tools {
  margin-top: 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.ai-tool {
  font-size: 11px;
  color: #888;
  background: #eef;
  border-radius: 6px;
  padding: 1px 6px;
}
.ai-products {
  margin-top: 6px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.ai-product {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border: 1px solid #ffd9d6;
  border-radius: 10px;
  padding: 6px 10px;
  cursor: pointer;
  transition: box-shadow 0.15s;
}
.ai-product:hover {
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.18);
}
.ai-product-thumb {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: linear-gradient(135deg, #ff7a45, #ff4d4f);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
}
.ai-product-info {
  flex: 1;
  min-width: 0;
}
.ai-product-name {
  font-size: 13px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ai-product-price {
  font-size: 13px;
  color: #ff4d4f;
  font-weight: 700;
}
.ai-product-go {
  font-size: 12px;
  color: #ff7a45;
  flex-shrink: 0;
}
.ai-input {
  display: flex;
  border-top: 1px solid #eee;
  padding: 8px;
}
.ai-input input {
  flex: 1;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 8px 10px;
  outline: none;
  font-size: 14px;
}
.ai-input button {
  margin-left: 8px;
  border: none;
  background: #ff4d4f;
  color: #fff;
  border-radius: 8px;
  padding: 0 16px;
  cursor: pointer;
}
.ai-input button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.ai-hint {
  font-size: 11px;
  color: #bbb;
  text-align: center;
  padding: 4px 8px 8px;
}
</style>
