import React from 'react'

export default function RippleButton({
  as: Tag = 'button',
  className = '',
  children,
  variant = 'primary',
  ...props
}) {
  const base = 'ripple-btn relative inline-flex items-center justify-center gap-2 rounded-xl font-medium transition-transform active:scale-[0.98] disabled:opacity-50 disabled:pointer-events-none'
  const variants = {
    primary: 'bg-flow-gradient text-white shadow-panel hover:brightness-105 px-5 py-2.5',
    ghost: 'bg-white text-ink border border-ink-100 hover:border-flow-300 px-5 py-2.5',
    dark: 'bg-ink-gradient text-white px-5 py-2.5',
    danger: 'bg-coral text-white px-5 py-2.5 hover:brightness-105',
    subtle: 'bg-flow-100 text-flow-700 px-4 py-2 text-sm'
  }

  function handleClick(e) {
    const btn = e.currentTarget
    const rect = btn.getBoundingClientRect()
    const size = Math.max(rect.width, rect.height)
    const span = document.createElement('span')
    span.className = 'ripple-span'
    span.style.width = span.style.height = `${size}px`
    span.style.left = `${e.clientX - rect.left - size / 2}px`
    span.style.top = `${e.clientY - rect.top - size / 2}px`
    btn.appendChild(span)
    setTimeout(() => span.remove(), 620)
    props.onClick?.(e)
  }

  return (
    <Tag className={`${base} ${variants[variant]} ${className}`} {...props} onClick={handleClick}>
      {children}
    </Tag>
  )
}
