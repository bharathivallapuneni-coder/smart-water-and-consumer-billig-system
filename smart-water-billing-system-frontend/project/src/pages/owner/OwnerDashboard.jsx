import React, { useEffect, useState } from 'react'
import { Users, Gauge, Receipt, Wallet } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { fetchResidents, fetchMeterReadings, fetchBillsForBuilding } from '../../api/services'
import { PageHeader, StatCard, Loader } from '../../components/UiBits'
import Panel from '../../components/Panel'
import StatusPill from '../../components/StatusPill'

export default function OwnerDashboard() {
  const { user } = useAuth()
  const [data, setData] = useState(null)

  useEffect(() => {
    Promise.all([fetchResidents(user.id), fetchMeterReadings(user.id), fetchBillsForBuilding(user.id)]).then(
      ([residents, readings, bills]) => setData({ residents, readings, bills })
    )
  }, [user.id])

  if (!data) return <Loader label="Loading dashboard" />

  const pendingBills = data.bills.filter((b) => b.status === 'PENDING')

  return (
    <div>
      <PageHeader eyebrow="Building Owner" title={user.buildingName} subtitle="Everything about your building's meters, bills and payments." />

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard icon={Users} label="Residents" value={data.residents.length} tone="flow" delay={0.02} />
        <StatCard icon={Gauge} label="Meter entries" value={data.readings.length} tone="ink" delay={0.06} />
        <StatCard icon={Receipt} label="Bills generated" value={data.bills.length} tone="flow" delay={0.1} />
        <StatCard icon={Wallet} label="Pending payments" value={pendingBills.length} tone="amber" delay={0.14} />
      </div>

      <div className="mt-6">
        <Panel title="Recent bills" eyebrow="Billing activity">
          {data.bills.length === 0 ? (
            <p className="text-sm text-slate">No bills generated yet. Head to Meter Entry to log a reading.</p>
          ) : (
            <div className="divide-y divide-ink-100/70">
              {data.bills.slice(0, 6).map((b) => {
                const resident = data.residents.find((r) => r.id === b.residentId)
                return (
                  <div key={b.id} className="flex items-center justify-between py-3 first:pt-0 last:pb-0">
                    <div>
                      <p className="font-display text-sm font-semibold text-ink">{resident?.flatNumber || 'Flat'}</p>
                      <p className="text-xs text-slate">{b.month} {b.year} · {b.usage} units</p>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="font-mono text-sm font-semibold text-ink">₹{b.amount}</span>
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
