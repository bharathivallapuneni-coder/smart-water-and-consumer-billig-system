import React, { useState } from 'react'
import { FileText, Droplets, Home, Calendar, CreditCard, Printer, CheckCircle, X, ShieldAlert } from 'lucide-react'
import { payInvoice } from '../api/services'

export default function ItemizedInvoiceModal({ invoice, isOpen, onClose, onPaidSuccess, isResident = false }) {
  const [paying, setPaying] = useState(false)
  const [error, setError] = useState(null)

  if (!isOpen || !invoice) return null

  const handlePay = async () => {
    setPaying(true)
    setError(null)
    try {
      await payInvoice(invoice.id, 'PAY_ONLINE_' + Date.now())
      if (onPaidSuccess) onPaidSuccess()
    } catch (err) {
      setError(err.message || 'Failed to process payment')
    } finally {
      setPaying(false)
    }
  }

  const handlePrint = () => {
    window.print()
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4 overflow-y-auto">
      <div className="bg-white dark:bg-slate-900 rounded-3xl shadow-2xl border border-slate-200 dark:border-slate-800 w-full max-w-2xl overflow-hidden relative my-8">
        {/* Header Header */}
        <div className="bg-gradient-to-r from-blue-600 to-indigo-700 p-6 text-white relative">
          <button
            onClick={onClose}
            className="absolute top-4 right-4 text-white/70 hover:text-white bg-white/10 hover:bg-white/20 rounded-full p-1.5 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
          <div className="flex items-center gap-3">
            <div className="p-3 bg-white/10 backdrop-blur-md rounded-2xl">
              <FileText className="w-7 h-7 text-white" />
            </div>
            <div>
              <span className="text-xs uppercase tracking-widest text-blue-200 font-semibold">Itemized Water Invoice</span>
              <h2 className="text-2xl font-bold">{invoice.invoiceNumber || 'INV-2026-001'}</h2>
            </div>
          </div>
        </div>

        {/* Content Body */}
        <div className="p-6 space-y-6">
          {error && (
            <div className="p-3 rounded-xl bg-rose-50 dark:bg-rose-950/40 text-rose-700 dark:text-rose-300 text-xs flex items-center gap-2">
              <ShieldAlert className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Info Header grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800 text-xs">
            <div>
              <span className="text-slate-400 block mb-0.5">Flat Number</span>
              <span className="font-bold text-slate-800 dark:text-slate-200 flex items-center gap-1">
                <Home className="w-3.5 h-3.5 text-blue-500" /> {invoice.flatNumber || 'A-101'}
              </span>
            </div>
            <div>
              <span className="text-slate-400 block mb-0.5">Billing Period</span>
              <span className="font-bold text-slate-800 dark:text-slate-200 flex items-center gap-1">
                <Calendar className="w-3.5 h-3.5 text-blue-500" /> {invoice.billingPeriod || 'July 2026'}
              </span>
            </div>
            <div>
              <span className="text-slate-400 block mb-0.5">Water Basis</span>
              <span className="font-bold text-slate-800 dark:text-slate-200 flex items-center gap-1">
                <Droplets className="w-3.5 h-3.5 text-blue-500" />
                {invoice.isMetered ? `${invoice.meteredConsumptionKl} kL (Metered)` : `${invoice.flatAreaSqft} sq ft (Flat Area)`}
              </span>
            </div>
            <div>
              <span className="text-slate-400 block mb-0.5">Status</span>
              <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full font-bold text-[10px] ${
                invoice.status === 'PAID'
                  ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-300'
                  : 'bg-amber-100 text-amber-700 dark:bg-amber-950/50 dark:text-amber-300'
              }`}>
                {invoice.status}
              </span>
            </div>
          </div>

          {/* Breakdown Table */}
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3">Itemized Cost Breakdown</h4>
            <div className="border border-slate-200 dark:border-slate-800 rounded-2xl overflow-hidden">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-100 dark:bg-slate-800/80 text-slate-600 dark:text-slate-300 font-semibold border-b border-slate-200 dark:border-slate-800">
                  <tr>
                    <th className="p-3">Charge Component</th>
                    <th className="p-3">Calculation Basis</th>
                    <th className="p-3 text-right">Amount (₹)</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-800 text-slate-700 dark:text-slate-300">
                  <tr>
                    <td className="p-3 font-medium text-slate-900 dark:text-white">Base Tiered Consumption Charge</td>
                    <td className="p-3 text-slate-500">First 10 kL @ ₹10, Above 10 kL @ ₹15</td>
                    <td className="p-3 text-right font-mono font-semibold">₹{Number(invoice.baseTieredCharge || 0).toFixed(2)}</td>
                  </tr>
                  <tr>
                    <td className="p-3 font-medium text-slate-900 dark:text-white">
                      Bulk Water Procurement Allocation
                      <span className="block text-[10px] text-slate-400 font-normal">
                        {invoice.isMetered ? 'Proportional to household metered usage' : 'Flat area proportional fallback'}
                      </span>
                    </td>
                    <td className="p-3 text-slate-500">Apartment procurement pool cost share</td>
                    <td className="p-3 text-right font-mono font-semibold">₹{Number(invoice.allocatedWaterProcurementCharge || 0).toFixed(2)}</td>
                  </tr>
                  <tr>
                    <td className="p-3 font-medium text-slate-900 dark:text-white">Shared Area Facilities Allocation</td>
                    <td className="p-3 text-slate-500">Garden, washrooms, & cleaning common share</td>
                    <td className="p-3 text-right font-mono font-semibold">₹{Number(invoice.sharedAreaCharge || 0).toFixed(2)}</td>
                  </tr>
                  {Number(invoice.adjustments) !== 0 && (
                    <tr>
                      <td className="p-3 font-medium text-slate-900 dark:text-white">Adjustments / Credits</td>
                      <td className="p-3 text-slate-500">Prior balance adjustment</td>
                      <td className="p-3 text-right font-mono font-semibold">₹{Number(invoice.adjustments || 0).toFixed(2)}</td>
                    </tr>
                  )}
                  <tr className="bg-blue-50/50 dark:bg-blue-950/20 font-bold text-sm">
                    <td className="p-3 text-blue-900 dark:text-blue-200" colSpan={2}>Total Amount Payable</td>
                    <td className="p-3 text-right text-blue-600 dark:text-blue-400 font-mono">₹{Number(invoice.totalAmount || 0).toFixed(2)}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          {/* Footer Actions */}
          <div className="flex items-center justify-between pt-2 border-t border-slate-100 dark:border-slate-800">
            <button
              onClick={handlePrint}
              className="px-4 py-2 bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 text-xs font-semibold rounded-xl transition-colors flex items-center gap-2"
            >
              <Printer className="w-4 h-4" /> Print Receipt
            </button>

            {invoice.status !== 'PAID' && (
              <button
                onClick={handlePay}
                disabled={paying}
                className="px-6 py-2.5 bg-blue-600 hover:bg-blue-700 text-white font-medium text-xs rounded-xl shadow-lg shadow-blue-500/25 transition-all flex items-center gap-2"
              >
                <CreditCard className="w-4 h-4" />
                {paying ? 'Processing...' : 'Pay Bill Now'}
              </button>
            )}

            {invoice.status === 'PAID' && (
              <div className="text-emerald-600 dark:text-emerald-400 text-xs font-semibold flex items-center gap-1.5 bg-emerald-50 dark:bg-emerald-950/50 px-3 py-1.5 rounded-xl border border-emerald-100 dark:border-emerald-900">
                <CheckCircle className="w-4 h-4" /> Payment Settled on {new Date(invoice.paidAt || invoice.generatedAt).toLocaleDateString()}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
