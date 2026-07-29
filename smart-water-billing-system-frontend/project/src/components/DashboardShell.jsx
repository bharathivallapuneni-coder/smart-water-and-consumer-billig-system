import React, { useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import { Droplets, LogOut, Menu, X, Bell } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

export default function DashboardShell({ navItems, roleLabel, children }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <div className="flex min-h-screen bg-foam">
      {/* Sidebar - desktop */}
      <aside className="hidden w-64 flex-col border-r border-ink-100 bg-ink-gradient px-4 py-6 text-white md:flex">
        <SidebarContent navItems={navItems} roleLabel={roleLabel} user={user} onLogout={handleLogout} />
      </aside>

      {/* Sidebar - mobile drawer */}
      <AnimatePresence>
        {open && (
          <>
            <motion.div
              className="fixed inset-0 z-40 bg-ink/50 md:hidden"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setOpen(false)}
            />
            <motion.aside
              className="fixed left-0 top-0 z-50 flex h-full w-64 flex-col bg-ink-gradient px-4 py-6 text-white md:hidden"
              initial={{ x: -280 }}
              animate={{ x: 0 }}
              exit={{ x: -280 }}
              transition={{ type: 'spring', stiffness: 320, damping: 32 }}
            >
              <button onClick={() => setOpen(false)} className="absolute right-4 top-5 text-white/70">
                <X size={18} />
              </button>
              <SidebarContent navItems={navItems} roleLabel={roleLabel} user={user} onLogout={handleLogout} onNav={() => setOpen(false)} />
            </motion.aside>
          </>
        )}
      </AnimatePresence>

      <div className="flex min-w-0 flex-1 flex-col">
        {/* Topbar */}
        <header className="flex items-center justify-between border-b border-ink-100 bg-white/80 px-5 py-3.5 backdrop-blur">
          <button className="text-ink md:hidden" onClick={() => setOpen(true)}>
            <Menu size={20} />
          </button>
          <div className="hidden md:block">
            <p className="panel-label">Welcome back</p>
            <p className="font-display text-sm font-semibold text-ink">{user?.name || user?.buildingName}</p>
          </div>
          <div className="flex items-center gap-3">
            <button className="relative rounded-full border border-ink-100 p-2 text-slate hover:text-flow-700">
              <Bell size={16} />
              <span className="absolute -right-0.5 -top-0.5 h-2 w-2 rounded-full bg-coral" />
            </button>
            <div className="hidden h-9 w-9 place-items-center rounded-full bg-flow-100 font-mono text-xs font-semibold text-flow-700 sm:grid">
              {(user?.name || 'U').slice(0, 1).toUpperCase()}
            </div>
          </div>
        </header>

        <main className="flex-1 px-4 py-6 sm:px-6 lg:px-8">{children}</main>
      </div>
    </div>
  )
}

function SidebarContent({ navItems, roleLabel, user, onLogout, onNav }) {
  return (
    <>
      <div className="mb-8 flex items-center gap-2 px-1">
        <div className="grid h-9 w-9 place-items-center rounded-xl bg-flow-500/20">
          <Droplets size={18} className="text-flow-300" />
        </div>
        <div>
          <p className="font-display text-[15px] font-semibold leading-none">HydroBill</p>
          <p className="mt-1 font-mono text-[10px] uppercase tracking-widest text-white/50">{roleLabel}</p>
        </div>
      </div>

      <nav className="flex-1 space-y-1">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            onClick={onNav}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors ${
                isActive ? 'bg-flow-500/20 text-flow-300' : 'text-white/65 hover:bg-white/5 hover:text-white'
              }`
            }
          >
            <item.icon size={17} />
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="mt-6 border-t border-white/10 pt-4">
        <p className="truncate font-mono text-xs text-white/60">{user?.buildingName || user?.flatNumber || user?.username}</p>
        <button onClick={onLogout} className="mt-3 flex w-full items-center gap-2 rounded-xl px-3 py-2 text-sm text-white/70 hover:bg-white/5 hover:text-coral">
          <LogOut size={16} />
          Log out
        </button>
      </div>
    </>
  )
}
