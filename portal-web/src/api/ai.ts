/**
 * AI 助手流式调用（SSE）。
 * 通过 vite 代理 /api -> 网关(8500) -> service-ai。
 */

export interface AiStatus {
  step: number
  tool: string
  args?: Record<string, unknown>
}

export interface ProductCard {
  skuId: number | string
  name?: string
  price?: number | string
}

export interface ChatHandlers {
  onStatus?: (status: AiStatus) => void
  onMessage?: (piece: string) => void
  onError?: (msg: string) => void
  onDone?: () => void
  onDoneEvent?: (data: { conversationId: string }) => void
  onProducts?: (cards: ProductCard[]) => void
}

export function chatStream(
  payload: { mode: string; message: string; conversationId?: string },
  handlers: ChatHandlers
): Promise<void> {
  const token = localStorage.getItem('token')
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  if (token) headers.token = token

  return fetch('/api/ai/chat', {
    method: 'POST',
    headers,
    body: JSON.stringify(payload)
  }).then((resp) => {
    if (!resp.ok) {
      throw new Error('请求失败：' + resp.status)
    }
    const reader = resp.body!.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    const parseChunk = (chunk: string) => {
      let event = 'message'
      let data = ''
      chunk.split('\n').forEach((line) => {
        if (line.startsWith('event:')) event = line.slice(6).trim()
        else if (line.startsWith('data:')) data += line.slice(5).trim()
      })
      if (!data) return
      try {
        if (event === 'status' && handlers.onStatus) handlers.onStatus(JSON.parse(data))
        else if (event === 'message' && handlers.onMessage) handlers.onMessage(data)
        else if (event === 'products' && handlers.onProducts) handlers.onProducts(JSON.parse(data))
        else if (event === 'done' && handlers.onDoneEvent) handlers.onDoneEvent(JSON.parse(data))
        else if (event === 'error' && handlers.onError) handlers.onError(data)
      } catch (e) {
        // 容错：非 JSON 时按消息处理
        if (event === 'message' && handlers.onMessage) handlers.onMessage(data)
      }
    }

    const pump = (): Promise<void> =>
      reader.read().then(({ done, value }) => {
        if (done) {
          handlers.onDone && handlers.onDone()
          return
        }
        buffer += decoder.decode(value, { stream: true })
        let idx: number
        while ((idx = buffer.indexOf('\n\n')) >= 0) {
          const chunk = buffer.slice(0, idx)
          buffer = buffer.slice(idx + 2)
          parseChunk(chunk)
        }
        return pump()
      })

    return pump()
  })
}

/** 清空某会话的全部历史（同时断开后端多轮上下文） */
export function deleteHistory(conversationId: string): Promise<void> {
  const token = localStorage.getItem('token')
  const headers: Record<string, string> = {}
  if (token) headers.token = token
  return fetch('/api/ai/history?conversationId=' + encodeURIComponent(conversationId), {
    method: 'DELETE',
    headers
  }).then((resp) => {
    if (!resp.ok) throw new Error('删除失败：' + resp.status)
  })
}
