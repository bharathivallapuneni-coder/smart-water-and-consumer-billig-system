import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import toast from 'react-hot-toast'
import { Droplets, ShieldCheck, Building2, Users, Eye, EyeOff } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import RippleButton from '../../components/RippleButton'
import { Field, inputClass } from '../../components/UiBits'

const roles = [
  { key: 'SUPER_ADMIN', label: 'Super Admin', icon: ShieldCheck, home: '/super-admin' },
  { key: 'BUILDING_OWNER', label: 'Building Owner', icon: Building2, home: '/owner' },
  { key: 'RESIDENT', label: 'Resident', icon: Users, home: '/resident' }
]

export default function Login() {
  const [role, setRole] = useState('BUILDING_OWNER')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPw, setShowPw] = useState(false)
  const [busy, setBusy] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const activeRole = roles.find((r) => r.key === role)

  async function handleSubmit(e) {
    e.preventDefault()
    setBusy(true)
    try {
      await login({ role, username, password })
      toast.success('Logged in successfully')
      navigate(activeRole.home)
    } catch (err) {
      toast.error(err.message || 'Login failed')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="grid min-h-screen md:grid-cols-2">
      {/* Left visual panel */}
      <div className="relative hidden flex-col justify-between bg-ink-gradient p-10 text-white md:flex">
        <Link to="/" className="flex items-center gap-2">
          <div className="grid h-9 w-9 place-items-center rounded-xl bg-flow-500/20">
            <Droplets size={18} className="text-flow-300" />
          </div>
          <span className="font-display text-lg font-semibold">HydroBill</span>
        </Link>
        <div>
          <p className="panel-label mb-3 text-flow-300">Role-based access</p>
          <h2 className="max-w-sm font-display text-3xl font-semibold leading-tight">
            One ledger. Three roles. Full accountability at every step.
          </h2>
          <p className="mt-4 max-w-sm text-sm text-white/60">
            Super Admin verifies buildings, owners manage residents and meters, residents pay and download receipts.
          </p>
        </div>
        <p className="font-mono text-xs text-white/40">© {new Date().getFullYear()} HydroBill</p>
      </div>

      {/* Right form panel */}
      <div className="flex items-center justify-center bg-foam px-6 py-12">
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.45 }}
          className="w-full max-w-sm"
        >
          <div className="mb-6 md:hidden">
            <div className="flex items-center gap-2">
              <div className="grid h-9 w-9 place-items-center rounded-xl bg-flow-gradient text-white">
                <Droplets size={18} />
              </div>
              <span className="font-display text-lg font-semibold text-ink">HydroBill</span>
            </div>
          </div>

          <h1 className="font-display text-2xl font-semibold text-ink">Welcome back</h1>
          <p className="mt-1 text-sm text-slate">Choose your role to continue</p>

          <div className="mt-5 grid grid-cols-3 gap-2">
            {roles.map((r) => (
              <button
                key={r.key}
                onClick={() => setRole(r.key)}
                className={`flex flex-col items-center gap-1.5 rounded-xl border px-2 py-3 text-[11px] font-medium transition-colors ${
                  role === r.key ? 'border-flow-500 bg-flow-100 text-flow-700' : 'border-ink-100 text-slate hover:border-flow-300'
                }`}
              >
                <r.icon size={16} />
                {r.label}
              </button>
            ))}
          </div>

          <form onSubmit={handleSubmit} className="mt-6 space-y-4">
            <Field label="Username">
              <input required value={username} onChange={(e) => setUsername(e.target.value)} className={inputClass} placeholder="e.g. ramesh.owner" />
            </Field>
            <Field label="Password">
              <div className="relative">
                <input
                  required
                  type={showPw ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className={inputClass}
                  placeholder="••••••••"
                />
                <button type="button" onClick={() => setShowPw((s) => !s)} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate">
                  {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </Field>

            <RippleButton type="submit" disabled={busy} className="w-full">
              {busy ? 'Signing in…' : 'Log in'}
            </RippleButton>
          </form>

          {role === 'BUILDING_OWNER' && (
            <p className="mt-5 text-center text-sm text-slate">
              New building owner?{' '}
              <Link to="/register" className="font-medium text-flow-700 hover:underline">
                Submit a registration request
              </Link>
            </p>
          )}

          
        </motion.div>
      </div>
    </div>
  )
}
