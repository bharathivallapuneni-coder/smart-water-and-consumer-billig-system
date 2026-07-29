import React, { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import { Receipt, Zap } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { fetchResidents, fetchMeterReadings, fetchBillsForBuilding, generateBill } from '../../api/services'
import { calculateBill } from '../../utils/tariff'
import { PageHeader, Loader, EmptyState } from '../../components/UiBits'
import Panel from '../../components/Panel'
import RippleButton from '../../components/RippleButton'
import StatusPill from '../../components/StatusPill'

export default function BillGeneration() {
  const { user } = useAuth()
  const [residents, setResidents] = useState([])
  const [readings, setReadings] = useState([])
  const [bills, setBills] = useState([])
  const [loading, setLoading] = useState(true)
  const [busyId, setBusyId] = useState(null)

  async function load() {
    setLoading(true)
    const [r, m, b] = await Promise.all([fetchResidents(user.id), fetchMeterReadings(user.id), fetchBillsForBuilding(user.id)])
    setResidents(r)
    setReadings(m)
    setBills(b)
    setLoading(false)
  }

  useEffect(() => {
    load()
  }, [user.id])

  const billedReadingIds = new Set(bills.map((b) => b.meterReadingId))
  const unbilled = readings.filter((r) => !billedReadingIds.has(r.id))

  async function handleGenerate(readingId) {
    setBusyId(readingId)
    try {
      const bill = await generateBill(user.id, readingId)
      setBills((prev) => [bill, ...prev])
      toast.success('Bill generated and sent to resident')
    } catch (err) {
      toast.error(err.message || 'Could not generate bill')
    } finally {
      setBusyId(null)
    }
  }

  if (loading) return <Loader label="Loading billing data" />

  return (
    <div>
      <PageHeader eyebrow="Billing" title="Bill generation" subtitle="Turn a logged meter reading into a resident bill using the fixed tariff." />

      <Panel title="Readings awaiting a bill" eyebrow={`${unbilled.length} pending`} className="mb-6">
        {unbilled.length === 0 ? (
          <EmptyState icon={Zap} title="All caught up" subtitle="Every logged reading already has a bill." />
        ) : (
          <div className="divide-y divide-ink-100/70">
            {unbilled.map((r) => {
              const resident = residents.find((x) => x.id === r.residentId)
              return (
                <div key={r.id} className="flex items-center justify-between py-3.5 first:pt-0 last:pb-0">
                  <div>
                    <p className="font-display text-sm font-semibold text-ink">{resident?.flatNumber} · {resident?.name}</p>
                    <p className="text-xs text-slate">{r.month} {r.year} · {r.previousReading} → {r.currentReading} ({r.usage} units)</p>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="font-mono text-sm font-semibold text-flow-700">₹{calculateBill(r.usage)}</span>
                    <RippleButton variant="subtle" disabled={busyId === r.id} onClick={() => handleGenerate(r.id)}>
                      <Receipt size={14} /> {busyId === r.id ? 'Generating…' : 'Generate bill'}
                    </RippleButton>
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </Panel>

      <Panel title="Generated bills" eyebrow="All cycles">
        {bills.length === 0 ? (
          <p className="text-sm text-slate">No bills generated yet.</p>
        ) : (
          <div className="divide-y divide-ink-100/70">
            {bills.map((b) => {
              const resident = residents.find((x) => x.id === b.residentId)
              return (
                <div key={b.id} className="flex items-center justify-between py-3 first:pt-0 last:pb-0">
                  <div>
                    <p className="font-display text-sm font-semibold text-ink">{resident?.flatNumber}</p>
                    <p className="text-xs text-slate">{b.month} {b.year} · {b.usage} units</p>
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
  )
}
