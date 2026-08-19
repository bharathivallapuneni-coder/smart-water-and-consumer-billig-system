import React from 'react'

export default function SkeletonLoader({ type = 'card', count = 3 }) {
  if (type === 'table') {
    return (
      <div className="w-full space-y-3 animate-pulse">
        <div className="h-10 bg-slate-200 dark:bg-slate-800 rounded-xl w-full" />
        {Array.from({ length: count }).map((_, i) => (
          <div key={i} className="h-14 bg-slate-100 dark:bg-slate-800/60 rounded-xl w-full" />
        ))}
      </div>
    )
  }

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 animate-pulse">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="h-36 rounded-2xl bg-slate-100 dark:bg-slate-800/60 border border-slate-200/60 dark:border-slate-800 p-5 space-y-3">
          <div className="h-4 bg-slate-200 dark:bg-slate-700 rounded-lg w-1/3" />
          <div className="h-6 bg-slate-200 dark:bg-slate-700 rounded-lg w-2/3" />
          <div className="h-4 bg-slate-200 dark:bg-slate-700 rounded-lg w-1/2" />
        </div>
      ))}
    </div>
  )
}
