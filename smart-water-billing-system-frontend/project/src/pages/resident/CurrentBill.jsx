import React, { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import { Wallet, Download, CheckCircle2 } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { fetchResidentBills, markBillPaid } from '../../api/services'
import { openRazorpayCheckout } from '../../utils/razorpay'
import { downloadReceipt } from '../../utils/receipt'
import { PageHeader, Loader, EmptyState } from '../../components/UiBits'
import Panel from '../../components/Panel'
import StatusPill from '../../components/StatusPill'
import RippleButton from '../../components/RippleButton'

export default function CurrentBill() {
  const { user } = useAuth()
  const [bills, setBills] = useState([])
  const [loading, setLoading] = useState(true)
  const [payingId, setPayingId] = useState(null)

  async function load() {
    setLoading(true)
    try {
      const res = await fetchResidentBills(user?.id)
      setBills(Array.isArray(res) ? res : [])
    } catch (e) {
      setBills([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [user?.id])

  const safeBills = Array.isArray(bills) ? bills : []
  const pending = safeBills.filter((b) => b?.status === 'PENDING')

  async function handlePay(bill) {
    setPayingId(bill.id)
    await openRazorpayCheckout({
      bill,
      resident: user,
      mock: true, // set false once VITE_RAZORPAY_KEY_ID + backend order/verify endpoints are live
      onSuccess: async ({ paymentId }) => {
        try {
          const updated = await markBillPaid(bill.id, paymentId)
          setBills((prev) => prev.map((b) => (b.id === bill.id ? updated : b)))
          toast.success('Payment successful')
        } catch (err) {
          toast.error(err.message || 'Could not confirm payment')
        } finally {
          setPayingId(null)
        }
      },
      onFailure: (err) => {
        toast.error(err.message || 'Payment failed')
        setPayingId(null)
      }
    })
  }

  if (loading) return <Loader label="Loading your bills" />

  return (
    <div>
      <PageHeader eyebrow="Flat billing" title="Current bill" subtitle="Pay instantly via UPI, cards or wallets through Razorpay." />

      {pending.length === 0 ? (
        <EmptyState icon={CheckCircle2} title="Nothing due right now" subtitle="You're all caught up on payments." />
      ) : (
        <div className="grid gap-5 sm:grid-cols-2">
          {pending.map((bill) => (
            <Panel key={bill.id}>
              <div className="flex items-start justify-between">
                <div>
                  <p className="panel-label">{bill.month} {bill.year}</p>
                  <p className="mt-1 font-mono text-3xl font-semibold text-ink">₹{bill.amount}</p>
                  <p className="mt-1 text-xs text-slate">{bill.usage} units consumed</p>
                </div>
                <StatusPill status={bill.status} />
              </div>
              <RippleButton className="mt-5 w-full" disabled={payingId === bill.id} onClick={() => handlePay(bill)}>
                <Wallet size={16} /> {payingId === bill.id ? 'Processing…' : 'Pay now'}
              </RippleButton>
            </Panel>
          ))}
        </div>
      )}

      <div className="mt-8">
        <Panel title="Recently paid" eyebrow="Download receipts">
          {bills.filter((b) => b.status === 'PAID').length === 0 ? (
            <p className="text-sm text-slate">No paid bills yet.</p>
          ) : (
            <div className="divide-y divide-ink-100/70">
              {bills
                .filter((b) => b.status === 'PAID')
                .map((b) => (
                  <div key={b.id} className="flex items-center justify-between py-3 first:pt-0 last:pb-0">
                    <div>
                      <p className="font-display text-sm font-semibold text-ink">{b.month} {b.year}</p>
                      <p className="text-xs text-slate">₹{b.amount} · paid {new Date(b.paidAt).toLocaleDateString('en-IN')}</p>
                    </div>
                    <RippleButton variant="subtle" onClick={() => downloadReceipt(b, user)}>
                      <Download size={13} /> Receipt
                    </RippleButton>
                  </div>
                ))}
            </div>
          )}
        </Panel>
      </div>
    </div>
  )
}
