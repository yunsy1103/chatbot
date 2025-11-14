import { useState, useRef, useEffect } from 'react'
import Sidebar from './components/Sidebar'
import ChatWindow from './components/ChatWindow'
import MessageInput from './components/MessageInput'

export default function App() {
    const [conversations, setConversations] = useState([])
    const [currentConversationId, setCurrentConversationId] = useState(null)
    const [isLoading, setIsLoading] = useState(false)
    const messagesEndRef = useRef(null)

    const currentConversation = conversations.find(c => c.id === currentConversationId)

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
    }

    useEffect(() => {
        scrollToBottom()
    }, [currentConversation?.messages])

    const createNewConversation = () => {
        const newConversation = {
            id: Date.now().toString(),
            title: 'New Conversation',
            messages: [],
            createdAt: new Date()
        }
        setConversations([newConversation, ...conversations])
        setCurrentConversationId(newConversation.id)
    }

    const deleteConversation = (id) => {
        const filtered = conversations.filter(c => c.id !== id)
        setConversations(filtered)
        if (currentConversationId === id) {
            setCurrentConversationId(filtered[0]?.id || null)
        }
    }

    const handleSendMessage = async (message) => {
        if (!currentConversationId) {
            createNewConversation()
        }

        const conversationId = currentConversationId || Date.now().toString()

        setConversations(prev =>
            prev.map(conv =>
                conv.id === conversationId
                    ? {
                        ...conv,
                        messages: [
                            ...conv.messages,
                            {
                                id: Date.now().toString(),
                                role: 'user',
                                content: message,
                                timestamp: new Date()
                            }
                        ]
                    }
                    : conv
            )
        )

        setIsLoading(true)

        try {
            const response = await fetch('http://localhost:8080/api/chat/ask', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ question: message })
            })

            if (!response.ok) {
                throw new Error('API 요청 실패')
            }

            const data = await response.json()
            const aiResponse = data.answer || '응답을 받지 못했습니다.'

            setConversations(prev =>
                prev.map(conv =>
                    conv.id === conversationId
                        ? {
                            ...conv,
                            title: conv.messages.length === 0 ? message.substring(0, 30) : conv.title,
                            messages: [
                                ...conv.messages,
                                {
                                    id: (Date.now() + 1).toString(),
                                    role: 'assistant',
                                    content: aiResponse,
                                    timestamp: new Date()
                                }
                            ]
                        }
                        : conv
                )
            )
        } catch (error) {
            console.error('메시지 전송 오류:', error)

            setConversations(prev =>
                prev.map(conv =>
                    conv.id === conversationId
                        ? {
                            ...conv,
                            messages: [
                                ...conv.messages,
                                {
                                    id: (Date.now() + 1).toString(),
                                    role: 'assistant',
                                    content: '죄송합니다. 오류가 발생했습니다. 다시 시도해주세요.',
                                    timestamp: new Date()
                                }
                            ]
                        }
                        : conv
                )
            )
        } finally {
            setIsLoading(false)
        }
    }

    return (
        <div className="flex h-screen bg-gray-50">
            <Sidebar
                conversations={conversations}
                currentConversationId={currentConversationId}
                onSelectConversation={setCurrentConversationId}
                onNewConversation={createNewConversation}
                onDeleteConversation={deleteConversation}
            />

            <div className="flex-1 flex flex-col bg-white">
                {currentConversation ? (
                    <>
                        <ChatWindow
                            messages={currentConversation.messages}
                            isLoading={isLoading}
                            messagesEndRef={messagesEndRef}
                        />
                        <MessageInput onSendMessage={handleSendMessage} isLoading={isLoading} />
                    </>
                ) : (
                    <div className="flex-1 flex flex-col items-center justify-center gap-6 text-center px-4">
                        <div className="space-y-6">
                            <h1 className="text-5xl font-bold text-gray-900">안녕하세요! Perso.ai에 대해 알고 계신가요?</h1>
                            <p className="text-gray-600 text-lg">필요하신 내용을 말씀해주시면 바로 도와드릴게요.</p>
                        </div>
                        <button
                            onClick={createNewConversation}
                            className="mt-1 px-8 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors font-medium text-lg"
                        >
                            New Conversation
                        </button>
                    </div>
                )}
            </div>
        </div>
    )
}