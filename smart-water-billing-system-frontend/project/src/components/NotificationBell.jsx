import React, { useState, useEffect } from 'react'
import { Bell, ShieldAlert, Droplets, FileText, CheckCircle2, X } from 'lucide-react'
import { fetchNotifications, markNotificationAsRead } from '../api/services'

export default function NotificationBell({ forRole, forId }) {
  const [notifications, setNotifications] = useState([])
  const [isOpen, setIsOpen] = useState(false)

  useEffect(() => {
    loadNotifications()
    const interval = setInterval(loadNotifications, 15000)
    return () => clearInterval(interval)
  }, [forRole, forId])

  const loadNotifications = async () => {
    try {
      const data = await fetchNotifications(forRole, forId)
      setNotifications(data || [])
    } catch (e) {
      // Ignore network errors silently for bell polling
    }
  }

  const unreadCount = notifications.filter((n) => !n.read).length

  const handleMarkRead = async (id, e) => {
    e.stopPropagation()
    try {
      await markNotificationAsRead(id)
      setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)))
    } catch (err) {
      console.error(err)
    }
  }

  const getIcon = (type) => {
    switch (type) {
      case 'LEAK_ALERT':
        return <ShieldAlert className="w-4 h-4 text-rose-500" />
      case 'HIGH_USAGE':
        return <Droplets className="w-4 h-4 text-amber-500" />
      case 'INVOICE_GENERATED':
        return <FileText className="w-4 h-4 text-blue-500" />
      default:
        return <CheckCircle2 className="w-4 h-4 text-emerald-500" />
    }
  }

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="relative p-2 rounded-xl text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
        title="Notifications & Alerts"
      >
        <Bell className="w-5 h-5" />
        {unreadCount > 0 && (
          <span className="absolute top-1 right-1 w-4 h-4 bg-rose-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center animate-pulse">
            {unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 z-50 overflow-hidden">
          <div className="p-4 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between bg-slate-50 dark:bg-slate-800/50">
            <div className="flex items-center gap-2">
              <Bell className="w-4 h-4 text-blue-600 dark:text-blue-400" />
              <h4 className="text-xs font-bold text-slate-800 dark:text-white uppercase tracking-wider">Notifications</h4>
            </div>
            {unreadCount > 0 && (
              <span className="text-[10px] font-medium bg-blue-100 text-blue-700 dark:bg-blue-950 dark:text-blue-300 px-2 py-0.5 rounded-full">
                {unreadCount} unread
              </span>
            )}
          </div>

          <div className="max-h-80 overflow-y-auto divide-y divide-slate-100 dark:divide-slate-800">
            {notifications.length === 0 ? (
              <div className="p-6 text-center text-xs text-slate-400">No notifications available</div>
            ) : (
              notifications.map((n) => (
                <div
                  key={n.id}
                  className={`p-3.5 flex items-start gap-3 transition-colors ${
                    !n.read ? 'bg-blue-50/40 dark:bg-blue-950/20' : 'hover:bg-slate-50 dark:hover:bg-slate-800/50'
                  }`}
                >
                  <div className="mt-0.5 p-1.5 rounded-lg bg-slate-100 dark:bg-slate-800 shrink-0">
                    {getIcon(n.notificationType)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between gap-1">
                      <p className="text-xs font-bold text-slate-900 dark:text-white truncate">{n.title}</p>
                      <span className="text-[9px] text-slate-400 shrink-0">
                        {new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                    <p className="text-xs text-slate-600 dark:text-slate-300 mt-0.5 line-clamp-2">{n.message}</p>

                    {!n.read && (
                      <button
                        onClick={(e) => handleMarkRead(n.id, e)}
                        className="mt-1.5 text-[10px] text-blue-600 hover:text-blue-700 dark:text-blue-400 font-semibold"
                      >
                        Mark as read
                      </button>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  )
}
