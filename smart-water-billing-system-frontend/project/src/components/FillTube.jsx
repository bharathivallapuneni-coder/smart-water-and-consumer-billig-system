import React from 'react'

export default function FillTube({ pct = 0, height = 64, label }) {
  return (
    <div className="flex flex-col items-center gap-1.5">
      <div className="fill-tube" style={{ width: 22, height }}>
        <div
          className="fill-tube__level animate-rise"
          style={{ '--fill': `${pct}%` }}
        />
      </div>
      {label && <span className="font-mono text-[10px] text-slate">{label}</span>}
    </div>
  )
}
