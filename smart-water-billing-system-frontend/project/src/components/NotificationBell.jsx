import React, { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Bell, ShieldAlert, Droplets, FileText, CheckCircle2, ChevronRight } from 'lucide-react'
import { fetchResidentAlerts, fetchUnreadAlertCount, markAlertRead } from '../api/services'

export default function NotificationBell({ forRole, forId }) {
  const [alerts, setAlerts] = useState([])
  const [unreadCount, setUnreadCount] = useState(0)
  const [isOpen, setIsOpen] = useState(false)

  useEffect(() => {
    loadAlerts()
    const interval = setInterval(loadAlerts, 15000)
    return () => clearInterval(interval)
  }, [forRole, forId])

  const loadAlerts = async () => {
    try {
      const count = await fetchUnreadAlertCount()
      setUnreadCount(typeof count === 'number' ? count : 0)

      const list = await fetchResidentAlerts()
      setAlerts(Array.isArray(list) ? list : [])
    } catch (e) {
      setAlerts([])
      setUnreadCount(0)
    }
  }

  const handleMarkRead = async (id, e) => {
    e.stopPropagation()
    try {
      await markAlertRead(id)
      setAlerts((prev) => prev.map((a) => (a.id === id ? { ...a, isRead: true, read: true } : a)))
      setUnreadCount((prev) => Math.max(0, prev - 1))
    } catch (err) {
      console.error(err)
    }
  }

  const getIcon = (alertType, severity) => {
    if (alertType === 'POSSIBLE_WATER_LEAK') {
      return <ShieldAlert className="w-4 h-4 text-rose-500" />
    }
    if (severity === 'CRITICAL' || alertType === 'CRITICAL_HIGH_CONSUMPTION') {
      return <Droplets className="w-4 h-4 text-rose-500" />
    }
    if (alertType === 'HIGH_CONSUMPTION' || severity === 'WARNING') {
      return <Droplets className="w-4 h-4 text-amber-500" />
    }
    return <CheckCircle2 className="w-4 h-4 text-emerald-500" />
  }

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="relative p-2 rounded-xl text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-all active:scale-95"
        title="Alerts & Notifications"
      >
        <Bell className="w-5 h-5" />
        {unreadCount > 0 && (
          <span className="absolute top-1 right-1 px-1.5 py-0.2 min-w-[18px] h-[18px] bg-rose-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center animate-pulse shadow-sm">
            {unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 z-50 overflow-hidden transition-all">
          <div className="p-4 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between bg-slate-50 dark:bg-slate-800/50">
            <div className="flex items-center gap-2">
              <Bell className="w-4 h-4 text-blue-600 dark:text-blue-400" />
              <h4 className="text-xs font-bold text-slate-800 dark:text-white uppercase tracking-wider">Water Usage Alerts</h4>
            </div>
            {unreadCount > 0 && (
              <span className="text-[10px] font-medium bg-rose-100 text-rose-700 dark:bg-rose-950 dark:text-rose-300 px-2.5 py-0.5 rounded-full">
                {unreadCount} unread
              </span>
            )}
          </div>

          <div className="max-h-80 overflow-y-auto divide-y divide-slate-100 dark:divide-slate-800">
            {alerts.length === 0 ? (
              <div className="p-6 text-center text-xs text-slate-400">No alerts available</div>
            ) : (
              alerts.slice(0, 5).map((n) => {
                const isUnread = !n.isRead && !n.read
                return (
                  <div
                    key={n.id}
                    className={`p-3.5 flex items-start gap-3 transition-colors ${
                      isUnread ? 'bg-amber-50/40 dark:bg-amber-950/20' : 'hover:bg-slate-50 dark:hover:bg-slate-800/50'
                    }`}
                  >
                    <div className="mt-0.5 p-1.5 rounded-lg bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 shrink-0 shadow-xs">
                      {getIcon(n.alertType, n.severity)}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between gap-1">
                        <p className="text-xs font-bold text-slate-900 dark:text-white truncate">{n.title}</p>
                        {n.createdAt && (
                          <span className="text-[9px] text-slate-400 shrink-0">
                            {new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-slate-600 dark:text-slate-300 mt-0.5 line-clamp-2 leading-relaxed">{n.message}</p>

                      {isUnread && (
                        <button
                          onClick={(e) => handleMarkRead(n.id, e)}
                          className="mt-1.5 text-[10px] text-blue-600 hover:text-blue-700 dark:text-blue-400 font-semibold inline-flex items-center gap-1"
                        >
                          Mark as read
                        </button>
                      )}
                    </div>
                  </div>
                )
              })
            )}
          </div>

          <div className="p-3 border-t border-slate-100 dark:border-slate-800 bg-slate-50/80 dark:bg-slate-800/40 text-center">
            <Link
              to="/resident/alerts"
              onClick={() => setIsOpen(false)}
              className="text-xs font-semibold text-blue-600 hover:text-blue-700 dark:text-blue-400 inline-flex items-center gap-1 transition-colors"
            >
              View All Alerts <ChevronRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        </div>
      )}
    </div>
  )
}
