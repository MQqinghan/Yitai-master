const Layout = () => import('@/layout/index.vue')
const ConversationLog = () => import('@/views/ai/conversationLog.vue')

export default [
  {
    path: '/ai',
    component: Layout,
    name: 'AiCenter',
    meta: {
      title: 'AI 中心'
    },
    icon: 'icon-robot',
    children: [
      {
        path: 'conversations',
        name: 'AiConversations',
        component: ConversationLog,
        meta: {
          title: '会话记录'
        }
      }
    ]
  }
]
