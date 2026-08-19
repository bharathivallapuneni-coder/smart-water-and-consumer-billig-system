import React, { useState, useEffect } from 'react'
import { Truck, Plus, Trash2, Edit3, DollarSign, Droplets, CheckCircle2, AlertCircle, Loader2, Calendar } from 'lucide-react'
import { fetchBulkPurchases, createBulkPurchase, updateBulkPurchase, deleteBulkPurchase } from '../../api/services'

export default function BulkWaterPurchases({ buildingId }) {
  const [purchases, setPurchases] = useState([])
  const [loading, setLoading] = useState(true)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingId, setEditingId] = useState(null)

  const [sourceType, setSourceType] = useState('Tanker Delivery')
  const [supplierName, setSupplierName] = useState('')
  const [purchaseDate, setPurchaseDate] = useState(new Date().toISOString().split('T')[0])
  const [purchasedVolumeKl, setPurchasedVolumeKl] = useState('')
  const [totalCost, setTotalCost] = useState('')
  const [notes, setNotes] = useState('')

  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    loadPurchases()
  }, [buildingId])

  const loadPurchases = async () => {
    setLoading(true)
    try {
      const data = await fetchBulkPurchases(buildingId)
      setPurchases(data || [])
    } catch (err) {
      setError('Failed to load bulk water purchases')
    } finally {
      setLoading(false)
    }
  }

  const calculatedUnitCost = purchasedVolumeKl > 0 && totalCost >= 0
    ? (Number(totalCost) / Number(purchasedVolumeKl)).toFixed(2)
    : '0.00'

  const handleOpenAdd = () => {
    setEditingId(null)
    setSourceType('Tanker Delivery')
    setSupplierName('')
    setPurchaseDate(new Date().toISOString().split('T')[0])
    setPurchasedVolumeKl('')
    setTotalCost('')
    setNotes('')
    setIsModalOpen(true)
  }

  const handleOpenEdit = (p) => {
    setEditingId(p.id)
    setSourceType(p.sourceType || 'Tanker Delivery')
    setSupplierName(p.supplierName || '')
    setPurchaseDate(p.purchaseDate || new Date().toISOString().split('T')[0])
    setPurchasedVolumeKl(p.purchasedVolumeKl || '')
    setTotalCost(p.totalCost || '')
    setNotes(p.notes || '')
    setIsModalOpen(true)
  }

  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    setMessage(null)
    setError(null)
    try {
      const payload = {
        buildingId,
        sourceType,
        supplierName,
        purchaseDate,
        purchasedVolumeKl: Number(purchasedVolumeKl),
        totalCost: Number(totalCost),
        notes
      }

      if (editingId) {
        await updateBulkPurchase(editingId, payload)
        setMessage('Water purchase updated successfully')
      } else {
        await createBulkPurchase(payload)
        setMessage('Bulk water purchase recorded successfully')
      }
      setIsModalOpen(false)
      loadPurchases()
    } catch (err) {
      setError(err.message || 'Failed to save purchase')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this purchase record?')) return
    try {
      await deleteBulkPurchase(id)
      loadPurchases()
    } catch (err) {
      setError(err.message || 'Failed to delete record')
    }
  }

  const totalVolume = purchases.reduce((acc, p) => acc + Number(p.purchasedVolumeKl || 0), 0)
  const totalSpend = purchases.reduce((acc, p) => acc + Number(p.totalCost || 0), 0)

  if (loading) {
    return (
      <div className="p-8 text-center text-slate-400 flex items-center justify-center gap-2">
        <Loader2 className="w-5 h-5 animate-spin" /> Loading procurement history...
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Truck className="w-5 h-5 text-blue-600 dark:text-blue-400" /> Bulk Water Purchase Tracking
          </h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Record tanker deliveries, municipal bulk supply, and view total volume & procurement costs.
          </p>
        </div>

        <button
          onClick={handleOpenAdd}
          className="px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold rounded-xl shadow-lg shadow-blue-500/25 transition-all flex items-center gap-1.5 shrink-0"
        >
          <Plus className="w-4 h-4" /> Add Water Purchase
        </button>
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

      {/* Aggregate Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-blue-50 dark:bg-blue-950/50 flex items-center justify-center text-blue-600 dark:text-blue-400">
            <Droplets className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xs text-slate-400 font-medium">Total Volume Purchased</span>
            <h3 className="text-lg font-bold text-slate-900 dark:text-white">{totalVolume.toFixed(1)} kL</h3>
          </div>
        </div>

        <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-emerald-50 dark:bg-emerald-950/50 flex items-center justify-center text-emerald-600 dark:text-emerald-400">
            <DollarSign className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xs text-slate-400 font-medium">Total Procurement Cost</span>
            <h3 className="text-lg font-bold text-slate-900 dark:text-white">₹{totalSpend.toLocaleString()}</h3>
          </div>
        </div>

        <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-indigo-50 dark:bg-indigo-950/50 flex items-center justify-center text-indigo-600 dark:text-indigo-400">
            <Truck className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xs text-slate-400 font-medium">Avg Unit Cost per kL</span>
            <h3 className="text-lg font-bold text-slate-900 dark:text-white">
              ₹{totalVolume > 0 ? (totalSpend / totalVolume).toFixed(2) : '0.00'} / kL
            </h3>
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="border border-slate-200 dark:border-slate-800 rounded-2xl overflow-hidden bg-white dark:bg-slate-900 shadow-sm">
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-50 dark:bg-slate-800/80 text-slate-600 dark:text-slate-300 font-semibold border-b border-slate-200 dark:border-slate-800">
            <tr>
              <th className="p-3">Purchase Date</th>
              <th className="p-3">Source Type</th>
              <th className="p-3">Supplier Name</th>
              <th className="p-3">Volume (kL)</th>
              <th className="p-3">Total Cost (₹)</th>
              <th className="p-3">Unit Cost (₹/kL)</th>
              <th className="p-3 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 dark:divide-slate-800 text-slate-700 dark:text-slate-300">
            {purchases.length === 0 ? (
              <tr>
                <td colSpan={7} className="p-6 text-center text-slate-400">
                  No bulk water purchases recorded yet. Click 'Add Water Purchase' to add one.
                </td>
              </tr>
            ) : (
              purchases.map((p) => (
                <tr key={p.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/30">
                  <td className="p-3 font-medium flex items-center gap-1.5 text-slate-900 dark:text-white">
                    <Calendar className="w-3.5 h-3.5 text-blue-500" /> {p.purchaseDate}
                  </td>
                  <td className="p-3">
                    <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-blue-50 text-blue-700 dark:bg-blue-950 dark:text-blue-300">
                      {p.sourceType}
                    </span>
                  </td>
                  <td className="p-3 text-slate-600 dark:text-slate-300">{p.supplierName || 'N/A'}</td>
                  <td className="p-3 font-bold font-mono text-slate-900 dark:text-white">{p.purchasedVolumeKl} kL</td>
                  <td className="p-3 font-bold font-mono text-slate-900 dark:text-white">₹{p.totalCost}</td>
                  <td className="p-3 font-semibold font-mono text-emerald-600 dark:text-emerald-400">₹{p.unitCostPerKl} / kL</td>
                  <td className="p-3 text-right space-x-1">
                    <button
                      onClick={() => handleOpenEdit(p)}
                      className="p-1.5 text-slate-500 hover:text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-950/50 rounded-lg transition-colors"
                    >
                      <Edit3 className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => handleDelete(p.id)}
                      className="p-1.5 text-slate-500 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/50 rounded-lg transition-colors"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Modal Form */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4">
          <div className="bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 w-full max-w-md p-6">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">
              {editingId ? 'Edit Water Purchase' : 'Record Bulk Water Purchase'}
            </h3>

            <form onSubmit={handleSave} className="space-y-3">
              <div>
                <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">Water Source Type</label>
                <select
                  value={sourceType}
                  onChange={(e) => setSourceType(e.target.value)}
                  className="w-full px-3 py-2 text-sm rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none"
                >
                  <option value="TANKER">TANKER</option>
                  <option value="Tanker Delivery">Tanker Delivery</option>
                  <option value="Municipal Supply">Municipal Supply</option>
                  <option value="Borewell/Other">Borewell / Other</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">Supplier / Source Name</label>
                <input
                  type="text"
                  placeholder="e.g. Aqua Pure Tankers"
                  value={supplierName}
                  onChange={(e) => setSupplierName(e.target.value)}
                  className="w-full px-3 py-2 text-sm rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">Purchase Date</label>
                <input
                  type="date"
                  required
                  value={purchaseDate}
                  onChange={(e) => setPurchaseDate(e.target.value)}
                  className="w-full px-3 py-2 text-sm rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">Volume (kL)</label>
                  <input
                    type="number"
                    step="0.1"
                    min="0.1"
                    required
                    placeholder="e.g. 50"
                    value={purchasedVolumeKl}
                    onChange={(e) => setPurchasedVolumeKl(e.target.value)}
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none"
                  />
                </div>

                <div>
                  <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">Total Cost (₹)</label>
                  <input
                    type="number"
                    min="0"
                    required
                    placeholder="e.g. 5000"
                    value={totalCost}
                    onChange={(e) => setTotalCost(e.target.value)}
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none"
                  />
                </div>
              </div>

              {/* Auto calculated unit cost badge */}
              <div className="p-3 bg-blue-50 dark:bg-blue-950/40 rounded-xl flex items-center justify-between text-xs">
                <span className="text-blue-700 dark:text-blue-300 font-medium">Calculated Unit Cost:</span>
                <span className="font-bold font-mono text-blue-900 dark:text-white text-sm">₹{calculatedUnitCost} / kL</span>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">Notes / Reference</label>
                <textarea
                  rows={2}
                  placeholder="Optional notes or receipt voucher number"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  className="w-full px-3 py-2 text-sm rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none"
                />
              </div>

              <div className="flex items-center gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="w-1/3 py-2 bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 text-xs font-medium rounded-xl hover:bg-slate-200 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="w-2/3 py-2.5 bg-blue-600 hover:bg-blue-700 text-white font-medium text-xs rounded-xl shadow-lg shadow-blue-500/25 transition-all flex items-center justify-center gap-2"
                >
                  {saving ? <Loader2 className="w-4 h-4 animate-spin" /> : 'Save Purchase'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
