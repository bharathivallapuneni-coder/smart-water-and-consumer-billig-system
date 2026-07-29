import React, { useEffect, useState } from 'react'

/**
 * Circular "meter dial" — the recurring signature visual of this app.
 * pct: 0-100 fill. label: small caption under the number.
 */
export default function MeterDial({ pct = 0, value, unit = '', label, size = 108, tone = 'flow' }) {
  const [animated, setAnimated] = useState(0)
  useEffect(() => {
    const t = setTimeout(() => setAnimated(pct), 60)
    return () => clearTimeout(t)
  }, [pct])

  const toneColor = tone === 'amber' ? '#E8A33D' : tone === 'coral' ? '#E15B4F' : '#4AA8D8'

  return (
    <div className="flex flex-col items-center gap-2">
      <div
        className="meter-dial"
        style={{ '--pct': animated, '--size': `${size}px`, background: `conic-gradient(${toneColor} calc(var(--pct) * 1%), #E1EDED 0)` }}
      >
        <div className="flex flex-col items-center leading-none">
          <span className="font-mono font-semibold text-ink" style={{ fontSize: size * 0.19 }}>
            {value}
          </span>
          {unit && <span className="panel-label mt-0.5">{unit}</span>}
        </div>
      </div>
      {label && <span className="panel-label text-center">{label}</span>}
    </div>
  )
}
