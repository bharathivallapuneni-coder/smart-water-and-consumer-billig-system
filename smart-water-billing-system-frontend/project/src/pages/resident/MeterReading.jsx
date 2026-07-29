import React, { useEffect, useState } from 'react'
import { Gauge } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { fetchResidentReadings } from '../../api/services'
import { tierFillPercent } from '../../utils/tariff'
import { PageHeader, Loader, EmptyState } from '../../components/UiBits'
import Panel from '../../components/Panel'
import MeterDial from '../../components/MeterDial'

export default function MeterReading() {
  const { user } = useAuth()
  const [readings, setReadings] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchResidentReadings(user.id).then((r) => {
      setReadings(r)
      setLoading(false)
    })
  }, [user.id])

  if (loading) return <Loader label="Loading readings" />

  return (
    <div>
      <PageHeader eyebrow="Flat readings" title="Meter reading history" subtitle="Recorded by your building owner at the end of each month." />

      {readings.length === 0 ? (
        <EmptyState icon={Gauge} title="No readings yet" subtitle="Your building owner hasn't logged a reading for this cycle." />
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {readings.map((r, i) => (
            <Panel key={r.id} delay={i * 0.04}>
              <p className="panel-label mb-3">{r.month} {r.year}</p>
              <div className="flex items-center justify-between">
                <MeterDial pct={tierFillPercent(r.usage)} value={r.usage} unit="units" size={92} />
                <div className="text-right font-mono text-xs text-slate">
                  <p>{r.previousReading}</p>
                  <p className="text-ink-400">↓</p>
                  <p className="font-semibold text-ink">{r.currentReading}</p>
                </div>
              </div>
            </Panel>
          ))}
        </div>
      )}
    </div>
  )
}
