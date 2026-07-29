import React, { useEffect, useState } from 'react'
import { Download, Wallet } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { fetchResidentBills } from '../../api/services'
import { downloadReceipt } from '../../utils/receipt'
import { PageHeader, Loader, EmptyState } from '../../components/UiBits'
import Panel from '../../components/Panel'
import RippleButton from '../../components/RippleButton'

export default function PaymentHistory() {
  const { user } = useAuth()
  const [bills, setBills] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchResidentBills(user.id).then((b) => {
      setBills(b.filter((x) => x.status === 'PAID'))
      setLoading(false)
    })
  }, [user.id])

  if (loading) return <Loader label="Loading payment history" />

  return (
    <div>
      <PageHeader eyebrow="Transactions" title="Payment history" subtitle="Every successful payment, with a downloadable receipt." />

      {bills.length === 0 ? (
        <EmptyState icon={Wallet} title="No payments yet" subtitle="Paid bills will show up here." />
      ) : (
        <Panel className="!p-0">
          <div className="divide-y divide-ink-100/70">
            {bills.map((b) => (
              <div key={b.id} className="flex flex-col gap-2 p-5 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <p className="font-display text-sm font-semibold text-ink">{b.month} {b.year}</p>
                  <p className="mt-0.5 font-mono text-xs text-slate">{b.paymentId} · {new Date(b.paidAt).toLocaleString('en-IN')}</p>
                </div>
                <div className="flex items-center gap-3">
                  <span className="font-mono text-sm text-flow-700">₹{b.amount}</span>
                  <RippleButton variant="subtle" onClick={() => downloadReceipt(b, user)}>
                    <Download size={13} /> Receipt
                  </RippleButton>
                </div>
              </div>
            ))}
          </div>
        </Panel>
      )}
    </div>
  )
}
