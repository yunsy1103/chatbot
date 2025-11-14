export default function Sidebar({
                                    conversations,
                                    currentConversationId,
                                    onSelectConversation,
                                    onNewConversation,
                                    onDeleteConversation
                                }) {
    return (
        <aside className="w-64 bg-gray-900 flex flex-col p-3">
            <button
                onClick={onNewConversation}
                className="w-full p-3 mb-3 text-white border border-gray-700 rounded-lg hover:bg-gray-800 transition-colors font-medium"
            >
                + New Chat
            </button>

            <div className="flex-1 overflow-y-auto space-y-2">
                {conversations.map((conv) => (
                    <div
                        key={conv.id}
                        onClick={() => onSelectConversation(conv.id)}
                        className={`flex items-center justify-between p-3 rounded-lg cursor-pointer transition-colors ${
                            conv.id === currentConversationId
                                ? 'bg-gray-800 text-white'
                                : 'text-gray-400 hover:bg-gray-800'
                        }`}
                    >
                        <span className="flex-1 truncate text-sm">{conv.title}</span>
                        <button
                            onClick={(e) => {
                                e.stopPropagation()
                                onDeleteConversation(conv.id)
                            }}
                            className="ml-2 text-gray-500 hover:text-red-500 text-xl leading-none"
                        >
                            ×
                        </button>
                    </div>
                ))}
            </div>
        </aside>
    )
}