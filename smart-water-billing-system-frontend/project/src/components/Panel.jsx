import React from 'react'
import { motion } from 'framer-motion'

export default function Panel({ children, className = '', title, eyebrow, action, delay = 0 }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 14 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.45, delay, ease: [0.22, 1, 0.36, 1] }}
      className={`rounded-2xl border border-ink-100/70 bg-white shadow-panel ${className}`}
    >
      {(title || action) && (
        <div className="flex items-center justify-between border-b border-ink-100/70 px-5 py-4">
          <div>
            {eyebrow && <p className="panel-label mb-0.5">{eyebrow}</p>}
            {title && <h3 className="font-display text-[15px] font-semibold text-ink">{title}</h3>}
          </div>
          {action}
        </div>
      )}
      <div className="p-5">{children}</div>
    </motion.div>
  )
}
