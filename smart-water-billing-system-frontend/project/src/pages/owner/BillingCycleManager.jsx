import React, { useState, useEffect } from 'react'
import { Calendar, Play, Lock, Archive, FileText, CheckCircle2, AlertCircle, Loader2, Eye, ShieldCheck } from 'lucide-react'
import { fetchBuildingCycles, openBillingCycle, finalizeBillingCycle, fetchBillsForBuilding } from '../../api/services'
import ItemizedInvoiceModal from '../../components/ItemizedInvoiceModal'

export default function BillingCycleManager({ buildingId }) {
  const [cycles, setCycles] = useState([])
  const [invoices, setInvoices] = useState([])
  const [loading, setLoading] = useState(true)
  const [actionLoading, setActionLoading] = useState(false)
  const [message, setMessage] = useState(null)
  const [error, setError] = useState(null)

  const [selectedInvoice, setSelectedInvoice] = useState(null)
  const [isInvoiceModalOpen, setIsInvoiceModalOpen] = useState(false)

  // Open cycle form state
  const [newMonth, setNewMonth] = useState('August')
  const [newYear, setNewYear] = useState(2026)

  useEffect(() => {
    loadData()
  }, [buildingId])

  const loadData = async () => {
    setLoading(true)
    try {
      const [cyData, invData] = await Promise.all([
        fetchBuildingCycles(buildingId),
        fetchBillsForBuilding(buildingId)
      ])
      setCycles(cyData || [])
      setInvoices(invData || [])
    } catch (err) {
      setError('Failed to load billing cycle data')
    } finally {
      setLoading(false)
    }
  }

  const handleOpenCycle = async (e) => {
    e.preventDefault()
    setActionLoading(true)
    setMessage(null)
    setError(null)
    try {
      await openBillingCycle(buildingId, { month: newMonth, year: Number(newYear) })
      setMessage(`Billing cycle for ${newMonth} ${newYear} opened successfully!`)
      loadData()
    } catch (err) {
      setError(err.message || 'Failed to open cycle')
    } finally {
      setActionLoading(false)
    }
  }

  const handleFinalize = async (cycleId, month, year) => {
    if (!window.confirm(`Are you sure you want to finalize the billing cycle for ${month} ${year}? This will generate itemized invoices and lock cycle data.`)) {
      return
    }
    setActionLoading(true)
    setMessage(null)
    setError(null)
    try {
      await finalizeBillingCycle(buildingId, cycleId)
      setMessage(`Billing cycle for ${month} ${year} has been FINALIZED and invoices generated!`)
      loadData()
    } catch (err) {
      setError(err.message || 'Failed to finalize billing cycle')
    } finally {
      setActionLoading(false)
    }
  }

  const handleViewInvoice = (inv) => {
    setSelectedInvoice(inv)
    setIsInvoiceModalOpen(true)
  }

  if (loading) {
    return (
      <div className="p-8 text-center text-slate-400 flex items-center justify-center gap-2">
        <Loader2 className="w-5 h-5 animate-spin" /> Loading billing cycles...
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Calendar className="w-5 h-5 text-blue-600 dark:text-blue-400" /> Billing Cycle Management
          </h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Open monthly billing cycles, calculate cost distribution, preview & finalize invoices.
          </p>
        </div>
      </div>

      {message && (
        <div className="p-3.5 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300 text-xs flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 shrink-0" />
          <span>{message}</span>
        </div>
      )}

      {error && (
        <div className="p-3.5 rounded-xl bg-rose-50 dark:bg-rose-950/40 text-rose-700 dark:text-rose-300 text-xs flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Open New Cycle Card */}
      <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm">
        <h3 className="text-sm font-bold text-slate-900 dark:text-white mb-3 flex items-center gap-2">
          <Play className="w-4 h-4 text-blue-600" /> Open New Billing Cycle
        </h3>

        <form onSubmit={handleOpenCycle} className="flex flex-wrap items-center gap-3">
          <div>
            <select
              value={newMonth}
              onChange={(e) => setNewMonth(e.target.value)}
              className="px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none"
            >
              {['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'].map((m) => (
                <option key={m} value={m}>{m}</option>
              ))}
            </select>
          </div>

          <div>
            <input
              type="number"
              value={newYear}
              onChange={(e) => setNewYear(e.target.value)}
              className="w-24 px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none font-mono"
            />
          </div>

          <button
            type="submit"
            disabled={actionLoading}
            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold rounded-xl shadow-md transition-all flex items-center gap-1.5"
          >
            {actionLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : 'Open Cycle'}
          </button>
        </form>
      </div>

      {/* Cycles Table */}
      <div className="border border-slate-200 dark:border-slate-800 rounded-2xl overflow-hidden bg-white dark:bg-slate-900 shadow-sm">
        <div className="p-4 bg-slate-50 dark:bg-slate-800/60 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between">
          <h4 className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider">Billing Cycles</h4>
        </div>
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-100/50 dark:bg-slate-800/40 text-slate-600 dark:text-slate-300 font-semibold border-b border-slate-200 dark:border-slate-800">
            <tr>
              <th className="p-3">Period</th>
              <th className="p-3">Status</th>
              <th className="p-3">Opened Date</th>
              <th className="p-3">Finalized Date</th>
              <th className="p-3 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 dark:divide-slate-800 text-slate-700 dark:text-slate-300">
            {cycles.length === 0 ? (
              <tr>
                <td colSpan={5} className="p-6 text-center text-slate-400">
                  No billing cycles created yet. Use the form above to open a cycle.
                </td>
              </tr>
            ) : (
              cycles.map((c) => (
                <tr key={c.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/30">
                  <td className="p-3 font-bold text-slate-900 dark:text-white flex items-center gap-2">
                    <Calendar className="w-4 h-4 text-blue-500" /> {c.month} {c.year}
                  </td>
                  <td className="p-3">
                    <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full font-bold text-[10px] ${
                      c.status === 'FINALIZED'
                        ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-300'
                        : c.status === 'OPEN'
                        ? 'bg-blue-100 text-blue-700 dark:bg-blue-950/50 dark:text-blue-300'
                        : 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400'
                    }`}>
                      {c.status === 'FINALIZED' && <ShieldCheck className="w-3 h-3" />}
                      {c.status}
                    </span>
                  </td>
                  <td className="p-3 text-slate-500">{c.openedAt ? new Date(c.openedAt).toLocaleDateString() : 'N/A'}</td>
                  <td className="p-3 text-slate-500">{c.finalizedAt ? new Date(c.finalizedAt).toLocaleDateString() : '—'}</td>
                  <td className="p-3 text-right">
                    {c.status === 'OPEN' ? (
                      <button
                        onClick={() => handleFinalize(c.id, c.month, c.year)}
                        disabled={actionLoading}
                        className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-[11px] rounded-lg shadow-sm transition-all flex items-center gap-1.5 ml-auto"
                      >
                        <Lock className="w-3.5 h-3.5" /> Finalize Cycle
                      </button>
                    ) : (
                      <span className="text-[10px] text-slate-400 flex items-center justify-end gap-1 font-medium">
                        <Lock className="w-3 h-3 text-slate-400" /> Cycle Data Protected
                      </span>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Generated Invoices List */}
      <div className="border border-slate-200 dark:border-slate-800 rounded-2xl overflow-hidden bg-white dark:bg-slate-900 shadow-sm">
        <div className="p-4 bg-slate-50 dark:bg-slate-800/60 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between">
          <h4 className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider">Itemized Invoices History</h4>
        </div>
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-100/50 dark:bg-slate-800/40 text-slate-600 dark:text-slate-300 font-semibold border-b border-slate-200 dark:border-slate-800">
            <tr>
              <th className="p-3">Invoice #</th>
              <th className="p-3">Flat</th>
              <th className="p-3">Resident</th>
              <th className="p-3">Period</th>
              <th className="p-3">Amount (₹)</th>
              <th className="p-3">Status</th>
              <th className="p-3 text-right">View Itemized Details</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 dark:divide-slate-800 text-slate-700 dark:text-slate-300">
            {invoices.length === 0 ? (
              <tr>
                <td colSpan={7} className="p-6 text-center text-slate-400">
                  No invoices generated yet. Finalize a cycle above to generate itemized bills.
                </td>
              </tr>
            ) : (
              invoices.map((inv) => (
                <tr key={inv.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/30">
                  <td className="p-3 font-mono font-bold text-blue-600 dark:text-blue-400">{inv.invoiceNumber}</td>
                  <td className="p-3 font-semibold">{inv.flatNumber}</td>
                  <td className="p-3">{inv.residentName}</td>
                  <td className="p-3 text-slate-500">{inv.billingPeriod}</td>
                  <td className="p-3 font-bold font-mono text-slate-900 dark:text-white">₹{inv.totalAmount}</td>
                  <td className="p-3">
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                      inv.status === 'PAID' ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300' : 'bg-amber-100 text-amber-700 dark:bg-amber-950 dark:text-amber-300'
                    }`}>
                      {inv.status}
                    </span>
                  </td>
                  <td className="p-3 text-right">
                    <button
                      onClick={() => handleViewInvoice(inv)}
                      className="px-3 py-1 bg-slate-100 dark:bg-slate-800 hover:bg-blue-50 dark:hover:bg-blue-950 text-slate-700 dark:text-slate-300 hover:text-blue-600 text-xs font-medium rounded-lg transition-colors inline-flex items-center gap-1"
                    >
                      <Eye className="w-3.5 h-3.5" /> View Breakdown
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <ItemizedInvoiceModal
        invoice={selectedInvoice}
        isOpen={isInvoiceModalOpen}
        onClose={() => setIsInvoiceModalOpen(false)}
      />
    </div>
  )
}
