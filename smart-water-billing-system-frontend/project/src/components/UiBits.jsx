import React from 'react'
import { motion } from 'framer-motion'

export function PageHeader({ eyebrow, title, subtitle, action }) {
  return (
    <div className="mb-6 flex flex-col justify-between gap-3 sm:flex-row sm:items-end">
      <div>
        {eyebrow && <p className="panel-label mb-1">{eyebrow}</p>}
        <h1 className="font-display text-2xl font-semibold text-ink">{title}</h1>
        {subtitle && <p className="mt-1 text-sm text-slate">{subtitle}</p>}
      </div>
      {action}
    </div>
  )
}

export function StatCard({ icon: Icon, label, value, tone = 'flow', delay = 0 }) {
  const tones = {
    flow: 'bg-flow-100 text-flow-700',
    amber: 'bg-amber-100 text-amber-600',
    coral: 'bg-coral-100 text-coral',
    ink: 'bg-ink-50 text-ink-600'
  }
  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay }}
      className="flex items-center gap-4 rounded-2xl border border-ink-100/70 bg-white p-5 shadow-panel"
    >
      <div className={`grid h-11 w-11 shrink-0 place-items-center rounded-xl ${tones[tone]}`}>
        <Icon size={19} />
      </div>
      <div>
        <p className="panel-label">{label}</p>
        <p className="font-mono text-xl font-semibold text-ink">{value}</p>
      </div>
    </motion.div>
  )
}

export function EmptyState({ icon: Icon, title, subtitle }) {
  return (
    <div className="flex flex-col items-center justify-center gap-2 rounded-2xl border border-dashed border-ink-100 py-14 text-center">
      {Icon && (
        <div className="grid h-11 w-11 place-items-center rounded-full bg-foam-200 text-slate">
          <Icon size={18} />
        </div>
      )}
      <p className="font-display text-sm font-semibold text-ink">{title}</p>
      {subtitle && <p className="max-w-xs text-xs text-slate">{subtitle}</p>}
    </div>
  )
}

export function Loader({ label = 'Loading' }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-16">
      <div className="flex gap-1.5">
        {[0, 1, 2].map((i) => (
          <motion.span
            key={i}
            className="h-2.5 w-2.5 rounded-full bg-flow-500"
            animate={{ y: [0, -7, 0] }}
            transition={{ repeat: Infinity, duration: 0.9, delay: i * 0.12, ease: 'easeInOut' }}
          />
        ))}
      </div>
      <p className="panel-label">{label}</p>
    </div>
  )
}

export function Field({ label, children }) {
  return (
    <label className="block">
      <span className="panel-label mb-1.5 block">{label}</span>
      {children}
    </label>
  )
}

export const inputClass =
  'w-full rounded-xl border border-ink-100 bg-foam-100 px-3.5 py-2.5 text-sm text-ink placeholder:text-slate/60 focus:border-flow-500 focus:bg-white focus:outline-none transition-colors'
