import React from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Droplets, ShieldCheck, Building2, Users, ArrowRight, Gauge, Receipt, Wallet } from 'lucide-react'
import MeterDial from '../components/MeterDial'
import RippleButton from '../components/RippleButton'
import FillTube from '../components/FillTube'

const steps = [
  { icon: Building2, title: 'Building owner registers', text: 'Submits building, address and contact details for review.' },
  { icon: ShieldCheck, title: 'Super Admin verifies', text: 'Checks details and approves or rejects the request.' },
  { icon: Users, title: 'Residents are onboarded', text: 'Owner assigns flats and issues resident logins.' },
  { icon: Gauge, title: 'Meter reading is logged', text: 'Owner records the monthly reading for each flat.' },
  { icon: Receipt, title: 'Bill is calculated', text: 'Usage is measured against the tariff slab automatically.' },
  { icon: Wallet, title: 'Resident pays online', text: 'UPI, cards or wallets — receipt issued the moment it clears.' }
]

export default function Landing() {
  return (
    <div className="min-h-screen bg-foam">
      <nav className="mx-auto flex max-w-6xl items-center justify-between px-6 py-6">
        <div className="flex items-center gap-2">
          <div className="grid h-9 w-9 place-items-center rounded-xl bg-flow-gradient text-white">
            <Droplets size={18} />
          </div>
          <span className="font-display text-lg font-semibold text-ink">HydroBill</span>
        </div>
        <Link to="/login">
          <RippleButton variant="ghost" className="text-sm">
            Log in <ArrowRight size={15} />
          </RippleButton>
        </Link>
      </nav>

      {/* Hero */}
      <section className="mx-auto grid max-w-6xl items-center gap-12 px-6 py-14 md:grid-cols-2 md:py-20">
        <motion.div initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.55 }}>
          <p className="panel-label mb-4 text-flow-700">Metered · Verified · Billed</p>
          <h1 className="font-display text-4xl font-semibold leading-[1.08] text-ink sm:text-5xl">
            Water billing your apartment can actually trust.
          </h1>
          <p className="mt-5 max-w-md text-[15px] leading-relaxed text-slate">
            Every building is verified by a Super Admin before it goes live. Every resident bill traces back to a real
            meter reading and a fixed, transparent tariff — no guesswork, no disputes.
          </p>
          <div className="mt-8 flex flex-wrap items-center gap-3">
            <Link to="/register">
              <RippleButton variant="primary">
                Register your building <ArrowRight size={16} />
              </RippleButton>
            </Link>
            <Link to="/login">
              <RippleButton variant="ghost">I already have an account</RippleButton>
            </Link>
          </div>

          <div className="mt-10 flex items-end gap-6">
            <FillTube pct={20} label="0-5" />
            <FillTube pct={40} label="6-10" />
            <FillTube pct={65} label="11-20" />
            <FillTube pct={85} label="21-30" />
            <FillTube pct={100} label="31+" />
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, scale: 0.94 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.6, delay: 0.15 }}
          className="relative mx-auto grid w-full max-w-sm place-items-center rounded-3xl border border-ink-100 bg-white p-8 shadow-panel-lg"
        >
          <p className="panel-label mb-6 self-start">Flat A-101 · July reading</p>
          <MeterDial pct={72} value="145" unit="units total" size={180} />
          <div className="mt-6 grid w-full grid-cols-2 gap-3">
            <div className="rounded-xl bg-foam-200 p-3">
              <p className="panel-label">Usage</p>
              <p className="font-mono text-lg font-semibold text-ink">25 units</p>
            </div>
            <div className="rounded-xl bg-flow-100 p-3">
              <p className="panel-label text-flow-700">Bill</p>
              <p className="font-mono text-lg font-semibold text-flow-700">₹900</p>
            </div>
          </div>
        </motion.div>
      </section>

      {/* Workflow */}
      <section className="border-t border-ink-100 bg-white py-16">
        <div className="mx-auto max-w-6xl px-6">
          <p className="panel-label mb-2">How a bill comes to be</p>
          <h2 className="font-display text-2xl font-semibold text-ink">Six steps, one accountable trail</h2>
          <div className="mt-10 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {steps.map((s, i) => (
              <motion.div
                key={s.title}
                initial={{ opacity: 0, y: 14 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true, margin: '-60px' }}
                transition={{ duration: 0.4, delay: i * 0.05 }}
                className="rounded-2xl border border-ink-100 p-5"
              >
                <div className="mb-3 flex items-center justify-between">
                  <div className="grid h-9 w-9 place-items-center rounded-lg bg-flow-100 text-flow-700">
                    <s.icon size={16} />
                  </div>
                  <span className="font-mono text-xs text-ink-100/0 text-slate">0{i + 1}</span>
                </div>
                <p className="font-display text-sm font-semibold text-ink">{s.title}</p>
                <p className="mt-1 text-[13px] leading-relaxed text-slate">{s.text}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      <footer className="border-t border-ink-100 py-8 text-center">
      </footer>
    </div>
  )
}
