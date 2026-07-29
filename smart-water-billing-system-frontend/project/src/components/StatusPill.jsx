import React from 'react'

const styles = {
  PENDING: 'bg-amber-100 text-amber-600',
  APPROVED: 'bg-flow-100 text-flow-700',
  PAID: 'bg-flow-100 text-flow-700',
  ACCEPTED: 'bg-flow-100 text-flow-700',
  REJECTED: 'bg-coral-100 text-coral',
  OVERDUE: 'bg-coral-100 text-coral'
}

export default function StatusPill({ status }) {
  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 font-mono text-[11px] uppercase tracking-wide ${styles[status] || 'bg-ink-50 text-slate'}`}>
      <span className="h-1.5 w-1.5 rounded-full bg-current" />
      {status}
    </span>
  )
}
