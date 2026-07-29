import React, { useEffect, useMemo, useState } from 'react'
import toast from 'react-hot-toast'
import { Gauge } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { fetchResidents, fetchMeterReadings, submitMeterReading } from '../../api/services'
import { calculateUsage, calculateBill, tierFillPercent } from '../../utils/tariff'
import { PageHeader, Field, inputClass, Loader } from '../../components/UiBits'
import Panel from '../../components/Panel'
import RippleButton from '../../components/RippleButton'
import MeterDial from '../../components/MeterDial'

const months = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']

export default function MeterEntry() {
  const { user } = useAuth()
  const [residents, setResidents] = useState([])
  const [readings, setReadings] = useState([])
  const [loading, setLoading] = useState(true)
  const [residentId, setResidentId] = useState('')
  const [currentReading, setCurrentReading] = useState('')
  const [month, setMonth] = useState(months[new Date().getMonth()])
  const [year, setYear] = useState(new Date().getFullYear())
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    Promise.all([fetchResidents(user.id), fetchMeterReadings(user.id)]).then(([r, m]) => {
      setResidents(r)
      setReadings(m)
      if (r.length) setResidentId(r[0].id)
      setLoading(false)
    })
  }, [user.id])

  const lastReading = useMemo(() => {
    const mine = readings.filter((r) => r.residentId === residentId).sort((a, b) => b.createdAt - a.createdAt)
    return mine[0]?.currentReading ?? 0
  }, [readings, residentId])

  const usage = calculateUsage(lastReading, currentReading || lastReading)
  const bill = calculateBill(usage)
  const fillPct = tierFillPercent(usage)

  async function handleSubmit(e) {
    e.preventDefault()
    if (!residentId) return toast.error('Select a resident first')
    setBusy(true)
    try {
      const record = await submitMeterReading(user.id, {
        residentId,
        previousReading: lastReading,
        currentReading: Number(currentReading),
        month,
        year: Number(year)
      })
      setReadings((prev) => [record, ...prev])
      setCurrentReading('')
      toast.success('Meter reading recorded')
    } catch (err) {
      toast.error(err.message || 'Could not save reading')
    } finally {
      setBusy(false)
    }
  }

  if (loading) return <Loader label="Loading meter data" />

  return (
    <div>
      <PageHeader eyebrow="Monthly cycle" title="Meter entry" subtitle="Log this month's reading — usage and the bill preview update instantly." />

      <div className="grid gap-5 lg:grid-cols-[1.2fr_1fr]">
        <Panel title="New reading" eyebrow="Manual entry">
          <form onSubmit={handleSubmit} className="space-y-4">
            <Field label="Resident / Flat">
              <select className={inputClass} value={residentId} onChange={(e) => setResidentId(e.target.value)}>
                {residents.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.flatNumber} — {r.name}
                  </option>
                ))}
              </select>
            </Field>

            <div className="grid grid-cols-2 gap-4">
              <Field label="Month">
                <select className={inputClass} value={month} onChange={(e) => setMonth(e.target.value)}>
                  {months.map((m) => (
                    <option key={m}>{m}</option>
                  ))}
                </select>
              </Field>
              <Field label="Year">
                <input type="number" className={inputClass} value={year} onChange={(e) => setYear(e.target.value)} />
              </Field>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Field label="Previous reading">
                <input disabled className={`${inputClass} opacity-70`} value={lastReading} />
              </Field>
              <Field label="Current reading">
                <input
                  required
                  type="number"
                  min={lastReading}
                  className={inputClass}
                  value={currentReading}
                  onChange={(e) => setCurrentReading(e.target.value)}
                  placeholder={`> ${lastReading}`}
                />
              </Field>
            </div>

            <RippleButton type="submit" disabled={busy || !residents.length} className="w-full">
              <Gauge size={16} /> {busy ? 'Saving…' : 'Save reading'}
            </RippleButton>
          </form>
        </Panel>

        <Panel title="Live preview" eyebrow="This billing cycle" delay={0.05}>
          <div className="flex flex-col items-center gap-5">
            <MeterDial pct={fillPct} value={usage} unit="units used" size={140} tone={usage > 20 ? 'coral' : usage > 10 ? 'amber' : 'flow'} />
            <div className="w-full rounded-xl bg-foam-200 p-4 text-center">
              <p className="panel-label">Bill amount</p>
              <p className="font-mono text-2xl font-semibold text-ink">₹{bill}</p>
            </div>
          </div>
        </Panel>
      </div>
    </div>
  )
}
