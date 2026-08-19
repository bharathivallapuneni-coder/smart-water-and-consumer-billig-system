import React, { useState, useEffect } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { ShieldAlert, Droplets, CheckCircle2, Check, Filter, AlertTriangle, RefreshCw } from 'lucide-react'
import toast from 'react-hot-toast'
import { fetchResidentAlerts, markAlertRead, resolveAlert } from '../../api/services'
import { PageHeader } from '../../components/UiBits'
import PageTransition from '../../components/PageTransition'
import SkeletonLoader from '../../components/SkeletonLoader'

export default function ResidentAlerts() {
  const [alerts, setAlerts] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('ALL') // ALL, UNREAD, WARNING, CRITICAL, LEAK

  useEffect(() => {
    loadAlerts()
  }, [])

  const loadAlerts = async () => {
    setLoading(true)
    try {
      const data = await fetchResidentAlerts()
      setAlerts(Array.isArray(data) ? data : [])
    } catch (err) {
      toast.error('Failed to load water usage alerts')
    } finally {
      setLoading(false)
    }
  }

  const handleMarkRead = async (id) => {
    try {
      await markAlertRead(id)
      setAlerts((prev) => prev.map((a) => (a.id === id ? { ...a, isRead: true, read: true } : a)))
      toast.success('Alert marked as read')
    } catch (err) {
      toast.error('Could not mark alert as read')
    }
  }

  const handleResolve = async (id) => {
    try {
      await resolveAlert(id)
      setAlerts((prev) =>
        prev.map((a) => (a.id === id ? { ...a, isRead: true, read: true, isResolved: true, resolvedAt: Date.now() } : a))
      )
      toast.success('Water alert marked as resolved!')
    } catch (err) {
      toast.error('Could not resolve alert')
    }
  }

  const filteredAlerts = alerts.filter((item) => {
    const isUnread = !item.isRead && !item.read
    if (filter === 'UNREAD') return isUnread
    if (filter === 'WARNING') return item.severity === 'WARNING'
    if (filter === 'CRITICAL') return item.severity === 'CRITICAL'
    if (filter === 'LEAK') return item.alertType === 'POSSIBLE_WATER_LEAK' || item.notificationType === 'LEAK_ALERT'
    return true
  })

  const getSeverityBadge = (sev, type) => {
    if (type === 'POSSIBLE_WATER_LEAK') {
      return (
        <span className="px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider rounded-full bg-rose-100 text-rose-800 dark:bg-rose-950/80 dark:text-rose-300 border border-rose-300 dark:border-rose-800 flex items-center gap-1 shadow-xs animate-pulse">
          <ShieldAlert className="w-3 h-3" /> POSSIBLE LEAK
        </span>
      )
    }
    if (sev === 'CRITICAL' || type === 'CRITICAL_HIGH_CONSUMPTION') {
      return (
        <span className="px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider rounded-full bg-rose-100 text-rose-800 dark:bg-rose-950/80 dark:text-rose-300 border border-rose-300 dark:border-rose-800 flex items-center gap-1">
          <AlertTriangle className="w-3 h-3" /> CRITICAL
        </span>
      )
    }
    return (
      <span className="px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider rounded-full bg-amber-100 text-amber-800 dark:bg-amber-950/80 dark:text-amber-300 border border-amber-300 dark:border-amber-800 flex items-center gap-1">
        <AlertTriangle className="w-3 h-3" /> WARNING
      </span>
    )
  }

  return (
    <PageTransition className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <PageHeader eyebrow="Resident Dashboard" title="Water Usage Alerts" subtitle="Real-time notifications for high consumption and leak detection." />
        <button
          onClick={loadAlerts}
          className="px-3.5 py-2 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:border-blue-500 hover:text-blue-600 text-xs font-semibold rounded-xl transition-all shadow-xs flex items-center gap-1.5 shrink-0 self-start sm:self-auto active:scale-95"
        >
          <RefreshCw className="w-3.5 h-3.5" /> Refresh
        </button>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1 no-scrollbar">
        {[
          { id: 'ALL', label: 'All Alerts' },
          { id: 'UNREAD', label: 'Unread' },
          { id: 'WARNING', label: 'Warnings' },
          { id: 'CRITICAL', label: 'Critical' },
          { id: 'LEAK', label: 'Leak Detection' }
        ].map((tab) => (
          <button
            key={tab.id}
            onClick={() => setFilter(tab.id)}
            className={`px-4 py-2 text-xs font-semibold rounded-xl transition-all whitespace-nowrap active:scale-95 ${
              filter === tab.id
                ? 'bg-blue-600 text-white shadow-md shadow-blue-500/20'
                : 'bg-white dark:bg-slate-800 text-slate-600 dark:text-slate-300 border border-slate-200 dark:border-slate-700 hover:bg-slate-50'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {loading ? (
        <SkeletonLoader type="card" count={3} />
      ) : filteredAlerts.length === 0 ? (
        <div className="p-12 text-center rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-xs">
          <div className="w-12 h-12 rounded-2xl bg-blue-50 dark:bg-blue-950 text-blue-600 dark:text-blue-400 grid place-items-center mx-auto mb-3">
            <CheckCircle2 className="w-6 h-6" />
          </div>
          <h3 className="text-sm font-bold text-slate-900 dark:text-white">No alerts found</h3>
          <p className="text-xs text-slate-500 mt-1">You are up to date! No active water usage alerts in this view.</p>
        </div>
      ) : (
        <div className="space-y-4">
          <AnimatePresence mode="popLayout">
            {filteredAlerts.map((alert, index) => {
              const isUnread = !alert.isRead && !alert.read
              const isResolved = alert.isResolved
              const isLeak = alert.alertType === 'POSSIBLE_WATER_LEAK' || alert.notificationType === 'LEAK_ALERT'

              return (
                <motion.div
                  key={alert.id}
                  layout
                  initial={{ opacity: 0, y: 14 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, scale: 0.95 }}
                  transition={{ duration: 0.2, delay: index * 0.05 }}
                  className={`p-5 sm:p-6 rounded-2xl border transition-all shadow-sm ${
                    isLeak
                      ? 'bg-rose-50/50 dark:bg-rose-950/20 border-rose-300 dark:border-rose-900 shadow-rose-500/5'
                      : isUnread
                      ? 'bg-amber-50/40 dark:bg-amber-950/20 border-amber-300 dark:border-amber-900'
                      : 'bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800'
                  }`}
                >
                  <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
                    <div className="flex items-start gap-3.5">
                      <div
                        className={`p-2.5 rounded-xl shrink-0 mt-0.5 ${
                          isLeak
                            ? 'bg-rose-500 text-white shadow-md shadow-rose-500/30'
                            : alert.severity === 'CRITICAL'
                            ? 'bg-rose-500 text-white'
                            : 'bg-amber-500 text-white'
                        }`}
                      >
                        {isLeak ? <ShieldAlert className="w-5 h-5" /> : <Droplets className="w-5 h-5" />}
                      </div>

                      <div className="space-y-1">
                        <div className="flex flex-wrap items-center gap-2">
                          <h3 className="font-display text-sm font-bold text-slate-900 dark:text-white">{alert.title}</h3>
                          {getSeverityBadge(alert.severity, alert.alertType)}

                          {isResolved ? (
                            <span className="px-2.5 py-0.5 text-[10px] font-semibold rounded-full bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300 border border-emerald-300 dark:border-emerald-800">
                              RESOLVED
                            </span>
                          ) : isUnread ? (
                            <span className="px-2.5 py-0.5 text-[10px] font-bold rounded-full bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-300">
                              NEW UNREAD
                            </span>
                          ) : null}
                        </div>

                        <p className="text-xs text-slate-700 dark:text-slate-300 leading-relaxed whitespace-pre-line mt-1.5">{alert.message}</p>

                        {/* Tariff & Consumption Metric Highlights */}
                        <div className="mt-4 flex flex-wrap items-center gap-3 pt-3 border-t border-slate-200/60 dark:border-slate-800/80">
                          {alert.currentConsumption != null && (
                            <div className="px-3 py-1.5 rounded-xl bg-slate-100 dark:bg-slate-800/80 text-xs font-semibold text-slate-800 dark:text-slate-200">
                              Current Usage: <span className="font-mono font-bold text-blue-600 dark:text-blue-400">{alert.currentConsumption} kL</span>
                            </div>
                          )}

                          {alert.tariffTier && (
                            <div className="px-3 py-1.5 rounded-xl bg-blue-50 dark:bg-blue-950/60 text-xs font-semibold text-blue-800 dark:text-blue-300 border border-blue-200 dark:border-blue-800">
                              Applicable Tier: <span className="font-bold">{alert.tariffTier}</span>
                            </div>
                          )}

                          {alert.averageConsumption != null && (
                            <div className="px-3 py-1.5 rounded-xl bg-purple-50 dark:bg-purple-950/60 text-xs font-semibold text-purple-800 dark:text-purple-300 border border-purple-200 dark:border-purple-800">
                              Historical Average: <span className="font-mono font-bold">{alert.averageConsumption} kL</span>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>

                    {/* Action buttons */}
                    <div className="flex sm:flex-col items-center sm:items-end gap-2 shrink-0 self-end sm:self-start pt-2 sm:pt-0">
                      {alert.createdAt && (
                        <span className="text-[10px] text-slate-400 font-medium mb-1">
                          {new Date(alert.createdAt).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}
                        </span>
                      )}

                      <div className="flex items-center gap-2">
                        {isUnread && (
                          <button
                            onClick={() => handleMarkRead(alert.id)}
                            className="px-3 py-1.5 bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 text-xs font-semibold rounded-xl transition-all flex items-center gap-1 active:scale-95"
                          >
                            <Check className="w-3.5 h-3.5 text-blue-500" /> Mark as Read
                          </button>
                        )}

                        {!isResolved && (
                          <button
                            onClick={() => handleResolve(alert.id)}
                            className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-xl transition-all shadow-sm flex items-center gap-1 active:scale-95"
                          >
                            <CheckCircle2 className="w-3.5 h-3.5" /> Resolve
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                </motion.div>
              )
            })}
          </AnimatePresence>
        </div>
      )}
    </PageTransition>
  )
}
