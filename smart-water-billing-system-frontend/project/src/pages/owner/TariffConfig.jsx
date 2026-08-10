import React, { useState, useEffect } from 'react'
import { Sliders, Plus, Trash2, Save, CheckCircle2, AlertCircle, Loader2 } from 'lucide-react'
import { fetchBuildingTariff, saveBuildingTariff } from '../../api/services'

export default function TariffConfig({ buildingId }) {
  const [tiers, setTiers] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    loadTariffs()
  }, [buildingId])

  const loadTariffs = async () => {
    setLoading(true)
    try {
      const data = await fetchBuildingTariff(buildingId)
      setTiers(data && data.length > 0 ? data : [
        { tierName: 'Base Tier (0-10 kL)', minKl: 0, maxKl: 10, ratePerKl: 10, fixedCharge: 0 },
        { tierName: 'High Tier (>10 kL)', minKl: 10, maxKl: null, ratePerKl: 15, fixedCharge: 0 }
      ])
    } catch (err) {
      setError('Failed to load building tariff rates')
    } finally {
      setLoading(false)
    }
  }

  const handleTierChange = (index, field, value) => {
    const updated = [...tiers]
    updated[index][field] = value === '' ? null : Number(value)
    setTiers(updated)
  }

  const handleNameChange = (index, value) => {
    const updated = [...tiers]
    updated[index].tierName = value
    setTiers(updated)
  }

  const addTier = () => {
    const lastMax = tiers.length > 0 ? tiers[tiers.length - 1].maxKl || 20 : 0
    setTiers([
      ...tiers,
      { tierName: `Tier ${tiers.length + 1}`, minKl: lastMax, maxKl: null, ratePerKl: 20, fixedCharge: 0 }
    ])
  }

  const removeTier = (index) => {
    setTiers(tiers.filter((_, i) => i !== index))
  }

  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    setMessage(null)
    setError(null)
    try {
      await saveBuildingTariff(buildingId, tiers)
      setMessage('Tiered tariff rates configured and saved successfully!')
    } catch (err) {
      setError(err.message || 'Failed to save tariff configuration')
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="p-8 text-center text-slate-400 flex items-center justify-center gap-2">
        <Loader2 className="w-5 h-5 animate-spin" /> Loading tariff settings...
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Sliders className="w-5 h-5 text-blue-600 dark:text-blue-400" /> Tiered Tariff Configuration
          </h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Configure consumption tiers (e.g. 0–10 kL @ ₹10, &gt;10 kL @ ₹15) for your building.
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

      <form onSubmit={handleSave} className="space-y-4">
        <div className="border border-slate-200 dark:border-slate-800 rounded-2xl overflow-hidden bg-white dark:bg-slate-900 shadow-sm">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 dark:bg-slate-800/80 text-slate-600 dark:text-slate-300 font-semibold border-b border-slate-200 dark:border-slate-800">
              <tr>
                <th className="p-3">Tier Name</th>
                <th className="p-3">Min Volume (kL)</th>
                <th className="p-3">Max Volume (kL)</th>
                <th className="p-3">Rate per kL (₹)</th>
                <th className="p-3">Fixed Charge (₹)</th>
                <th className="p-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800 text-slate-700 dark:text-slate-300">
              {tiers.map((tier, idx) => (
                <tr key={idx} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/30">
                  <td className="p-3">
                    <input
                      type="text"
                      value={tier.tierName || ''}
                      onChange={(e) => handleNameChange(idx, e.target.value)}
                      className="w-full px-2.5 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-xs focus:ring-2 focus:ring-blue-500 focus:outline-none"
                    />
                  </td>
                  <td className="p-3">
                    <input
                      type="number"
                      min="0"
                      step="0.1"
                      value={tier.minKl ?? 0}
                      onChange={(e) => handleTierChange(idx, 'minKl', e.target.value)}
                      className="w-24 px-2.5 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-xs focus:ring-2 focus:ring-blue-500 focus:outline-none"
                    />
                  </td>
                  <td className="p-3">
                    <input
                      type="number"
                      placeholder="∞ (Unlimited)"
                      value={tier.maxKl ?? ''}
                      onChange={(e) => handleTierChange(idx, 'maxKl', e.target.value)}
                      className="w-28 px-2.5 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-xs focus:ring-2 focus:ring-blue-500 focus:outline-none"
                    />
                  </td>
                  <td className="p-3">
                    <input
                      type="number"
                      min="0"
                      step="0.5"
                      value={tier.ratePerKl ?? 0}
                      onChange={(e) => handleTierChange(idx, 'ratePerKl', e.target.value)}
                      className="w-24 px-2.5 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-xs focus:ring-2 focus:ring-blue-500 focus:outline-none font-bold text-blue-600 dark:text-blue-400"
                    />
                  </td>
                  <td className="p-3">
                    <input
                      type="number"
                      min="0"
                      value={tier.fixedCharge ?? 0}
                      onChange={(e) => handleTierChange(idx, 'fixedCharge', e.target.value)}
                      className="w-24 px-2.5 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-xs focus:ring-2 focus:ring-blue-500 focus:outline-none"
                    />
                  </td>
                  <td className="p-3 text-right">
                    {tiers.length > 1 && (
                      <button
                        type="button"
                        onClick={() => removeTier(idx)}
                        className="p-1.5 text-rose-500 hover:text-rose-700 hover:bg-rose-50 dark:hover:bg-rose-950/50 rounded-lg transition-colors"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="flex items-center justify-between pt-2">
          <button
            type="button"
            onClick={addTier}
            className="px-3.5 py-2 bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-200 text-xs font-semibold rounded-xl transition-colors flex items-center gap-1.5"
          >
            <Plus className="w-4 h-4" /> Add Additional Tier
          </button>

          <button
            type="submit"
            disabled={saving}
            className="px-6 py-2.5 bg-blue-600 hover:bg-blue-700 text-white font-medium text-xs rounded-xl shadow-lg shadow-blue-500/25 transition-all flex items-center gap-2"
          >
            {saving ? <Loader2 className="w-4 h-4 animate-spin" /> : <><Save className="w-4 h-4" /> Save Tariff Configuration</>}
          </button>
        </div>
      </form>
    </div>
  )
}
