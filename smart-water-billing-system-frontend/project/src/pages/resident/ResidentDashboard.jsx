import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Gauge, Receipt, Wallet, ShieldAlert, FileText, KeyRound, Lock, Droplets } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { fetchResidentBills, fetchResidentReadings, fetchNotifications } from '../../api/services'
import { tierFillPercent } from '../../utils/tariff'
import { PageHeader, Loader } from '../../components/UiBits'
import Panel from '../../components/Panel'
import MeterDial from '../../components/MeterDial'
import StatusPill from '../../components/StatusPill'
import RippleButton from '../../components/RippleButton'
import ItemizedInvoiceModal from '../../components/ItemizedInvoiceModal'
import ChangePasswordModal from '../../components/ChangePasswordModal'

export default function ResidentDashboard() {
  const { user } = useAuth()
  const [bills, setBills] = useState([])
  const [readings, setReadings] = useState([])
  const [alerts, setAlerts] = useState([])
  const [loading, setLoading] = useState(true)

  const [selectedInvoice, setSelectedInvoice] = useState(null)
  const [isInvoiceModalOpen, setIsInvoiceModalOpen] = useState(false)
  const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false)

  useEffect(() => {
    loadDashboardData()
  }, [user.id])

  const loadDashboardData = async () => {
    try {
      const [b, r, n] = await Promise.all([
        fetchResidentBills(user.id),
        fetchResidentReadings(user.id),
        fetchNotifications('RESIDENT', user.id)
      ])
      setBills(b || [])
      setReadings(r || [])
      setAlerts((n || []).filter((item) => item.notificationType === 'LEAK_ALERT' || item.notificationType === 'HIGH_USAGE'))
      setLoading(false)
    } catch (e) {
      setLoading(false)
    }
  }

  if (loading) return <Loader label="Loading your dashboard" />

  const currentInvoice = bills.find((b) => b.status === 'PENDING') || bills[0]
  const latestReading = readings[0]

  const handleOpenInvoice = (inv) => {
    setSelectedInvoice(inv)
    setIsInvoiceModalOpen(true)
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <PageHeader eyebrow={`Flat ${user.flatNumber}`} title={`Hi ${user.name.split(' ')[0]}`} subtitle={user.buildingName} />
        <button
          onClick={() => setIsPasswordModalOpen(true)}
          className="px-3.5 py-2 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:border-blue-500 hover:text-blue-600 text-xs font-semibold rounded-xl transition-all shadow-sm flex items-center gap-1.5 shrink-0 self-start sm:self-auto"
        >
          <KeyRound className="w-4 h-4 text-blue-500" /> Change Password
        </button>
      </div>

      {/* Outlier & Leak Alert Banner */}
      {alerts.length > 0 && (
        <div className="p-4 rounded-2xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900 text-rose-900 dark:text-rose-200 shadow-sm flex items-start gap-3">
          <div className="p-2 rounded-xl bg-rose-500 text-white shrink-0 mt-0.5">
            <ShieldAlert className="w-5 h-5" />
          </div>
          <div className="flex-1">
            <h4 className="text-xs font-bold uppercase tracking-wider text-rose-700 dark:text-rose-300">Water Consumption Alert</h4>
            <p className="text-xs mt-1 font-medium leading-relaxed">{alerts[0].message}</p>
          </div>
        </div>
      )}

      <div className="grid gap-5 lg:grid-cols-[1fr_1.4fr]">
        <Panel title="Latest meter reading" eyebrow={latestReading ? `${latestReading.month} ${latestReading.year}` : 'No data'}>
          <div className="flex flex-col items-center gap-4">
            <MeterDial
              pct={latestReading ? tierFillPercent(latestReading.usage) : 0}
              value={latestReading?.usage ?? 0}
              unit="kL used"
              size={150}
            />
            {latestReading && (
              <p className="text-center text-xs text-slate font-medium">
                {latestReading.previousReading} kL → {latestReading.currentReading} kL
              </p>
            )}
          </div>
        </Panel>

        <Panel title="Current Itemized Bill" eyebrow="This cycle" delay={0.05}>
          {currentInvoice ? (
            <div>
              <div className="flex items-center justify-between rounded-xl bg-foam-200 p-4">
                <div>
                  <p className="panel-label">{currentInvoice.billingPeriod || `${currentInvoice.month} ${currentInvoice.year}`}</p>
                  <p className="font-mono text-2xl font-semibold text-ink">₹{currentInvoice.totalAmount || currentInvoice.amount}</p>
                </div>
                <StatusPill status={currentInvoice.status} />
              </div>

              <div className="mt-4 flex gap-2">
                <button
                  onClick={() => handleOpenInvoice(currentInvoice)}
                  className="flex-1 py-2.5 bg-blue-50 dark:bg-blue-950/40 text-blue-700 dark:text-blue-300 hover:bg-blue-100 font-semibold text-xs rounded-xl border border-blue-200 dark:border-blue-800 transition-colors flex items-center justify-center gap-1.5"
                >
                  <FileText className="w-4 h-4" /> View Itemized Breakdown
                </button>

                {currentInvoice.status === 'PENDING' && (
                  <Link to="/resident/bill" className="flex-1">
                    <RippleButton className="w-full">
                      <Wallet size={16} /> Pay Now
                    </RippleButton>
                  </Link>
                )}
              </div>
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

      <ItemizedInvoiceModal
        invoice={selectedInvoice}
        isOpen={isInvoiceModalOpen}
        onClose={() => setIsInvoiceModalOpen(false)}
        onPaidSuccess={loadDashboardData}
        isResident={true}
      />

      <ChangePasswordModal
        isOpen={isPasswordModalOpen}
        onClose={() => setIsPasswordModalOpen(false)}
        userId={user.id}
      />
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
