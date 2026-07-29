import React, { useEffect, useState } from 'react'
import { Wallet } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { fetchResidents, fetchBillsForBuilding } from '../../api/services'
import { PageHeader, Loader, EmptyState, StatCard } from '../../components/UiBits'
import Panel from '../../components/Panel'
import StatusPill from '../../components/StatusPill'

export default function PaymentStatus() {
  const { user } = useAuth()
  const [residents, setResidents] = useState([])
  const [bills, setBills] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('ALL')

  useEffect(() => {
    Promise.all([fetchResidents(user.id), fetchBillsForBuilding(user.id)]).then(([r, b]) => {
      setResidents(r)
      setBills(b)
      setLoading(false)
    })
  }, [user.id])

  if (loading) return <Loader label="Loading payments" />

  const paid = bills.filter((b) => b.status === 'PAID')
  const pending = bills.filter((b) => b.status === 'PENDING')
  const collected = paid.reduce((sum, b) => sum + b.amount, 0)
  const outstanding = pending.reduce((sum, b) => sum + b.amount, 0)

  const visible = filter === 'ALL' ? bills : bills.filter((b) => b.status === filter)

  return (
    <div>
      <PageHeader eyebrow="Collections" title="Payment status" subtitle="Track what's been collected and what's still outstanding." />

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard icon={Wallet} label="Collected" value={`₹${collected}`} tone="flow" />
        <StatCard icon={Wallet} label="Outstanding" value={`₹${outstanding}`} tone="amber" delay={0.05} />
        <StatCard icon={Wallet} label="Paid bills" value={paid.length} tone="flow" delay={0.1} />
        <StatCard icon={Wallet} label="Pending bills" value={pending.length} tone="coral" delay={0.15} />
      </div>

      <div className="mt-6">
        <Panel
          title="All bills"
          eyebrow="Filter"
          action={
            <div className="flex gap-1.5">
              {['ALL', 'PAID', 'PENDING'].map((f) => (
                <button
                  key={f}
                  onClick={() => setFilter(f)}
                  className={`rounded-lg px-3 py-1.5 font-mono text-[11px] uppercase transition-colors ${
                    filter === f ? 'bg-ink text-white' : 'bg-foam-200 text-slate hover:bg-foam-300'
                  }`}
                >
                  {f}
                </button>
              ))}
            </div>
          }
        >
          {visible.length === 0 ? (
            <EmptyState icon={Wallet} title="Nothing here" subtitle="Try a different filter." />
          ) : (
            <div className="divide-y divide-ink-100/70">
              {visible.map((b) => {
                const resident = residents.find((r) => r.id === b.residentId)
                return (
                  <div key={b.id} className="flex items-center justify-between py-3 first:pt-0 last:pb-0">
                    <div>
                      <p className="font-display text-sm font-semibold text-ink">{resident?.flatNumber} · {resident?.name}</p>
                      <p className="text-xs text-slate">{b.month} {b.year}</p>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="font-mono text-sm text-ink">₹{b.amount}</span>
                      <StatusPill status={b.status} />
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </Panel>
      </div>
    </div>
  )
}
