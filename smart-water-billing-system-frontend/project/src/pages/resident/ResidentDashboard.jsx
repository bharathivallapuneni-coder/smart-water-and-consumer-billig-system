import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Gauge, Receipt, Wallet } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { fetchResidentBills, fetchResidentReadings } from '../../api/services'
import { tierFillPercent } from '../../utils/tariff'
import { PageHeader, Loader } from '../../components/UiBits'
import Panel from '../../components/Panel'
import MeterDial from '../../components/MeterDial'
import StatusPill from '../../components/StatusPill'
import RippleButton from '../../components/RippleButton'

export default function ResidentDashboard() {
  const { user } = useAuth()
  const [bills, setBills] = useState([])
  const [readings, setReadings] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([fetchResidentBills(user.id), fetchResidentReadings(user.id)]).then(([b, r]) => {
      setBills(b)
      setReadings(r)
      setLoading(false)
    })
  }, [user.id])

  if (loading) return <Loader label="Loading your dashboard" />

  const currentBill = bills.find((b) => b.status === 'PENDING') || bills[0]
  const latestReading = readings[0]

  return (
    <div>
      <PageHeader eyebrow={`Flat ${user.flatNumber}`} title={`Hi ${user.name.split(' ')[0]}`} subtitle={user.buildingName} />

      <div className="grid gap-5 lg:grid-cols-[1fr_1.4fr]">
        <Panel title="Latest meter reading" eyebrow={latestReading ? `${latestReading.month} ${latestReading.year}` : 'No data'}>
          <div className="flex flex-col items-center gap-4">
            <MeterDial
              pct={latestReading ? tierFillPercent(latestReading.usage) : 0}
              value={latestReading?.usage ?? 0}
              unit="units used"
              size={150}
            />
            {latestReading && (
              <p className="text-center text-xs text-slate">
                {latestReading.previousReading} → {latestReading.currentReading}
              </p>
            )}
          </div>
        </Panel>

        <Panel title="Current bill" eyebrow="This cycle" delay={0.05}>
          {currentBill ? (
            <div>
              <div className="flex items-center justify-between rounded-xl bg-foam-200 p-4">
                <div>
                  <p className="panel-label">{currentBill.month} {currentBill.year}</p>
                  <p className="font-mono text-2xl font-semibold text-ink">₹{currentBill.amount}</p>
                </div>
                <StatusPill status={currentBill.status} />
              </div>
              {currentBill.status === 'PENDING' ? (
                <Link to="/resident/bill">
                  <RippleButton className="mt-4 w-full">
                    <Wallet size={16} /> Pay now
                  </RippleButton>
                </Link>
              ) : (
                <p className="mt-4 text-center text-sm text-flow-700">Paid — thank you!</p>
              )}
            </div>
          ) : (
            <p className="text-sm text-slate">No bill generated yet for this cycle.</p>
          )}

          <div className="mt-5 grid grid-cols-3 gap-3">
            <QuickLink to="/resident/meter" icon={Gauge} label="Meter" />
            <QuickLink to="/resident/history" icon={Receipt} label="History" />
            <QuickLink to="/resident/payments" icon={Wallet} label="Payments" />
          </div>
        </Panel>
      </div>
    </div>
  )
}

function QuickLink({ to, icon: Icon, label }) {
  return (
    <Link to={to} className="flex flex-col items-center gap-1.5 rounded-xl border border-ink-100 py-3 text-slate transition-colors hover:border-flow-300 hover:text-flow-700">
      <Icon size={17} />
      <span className="text-[11px] font-medium">{label}</span>
    </Link>
  )
}
