import { useEffect, useState } from "react";
import apiClient from "../api/client";
import { useAuth } from "../context/AuthContext";

interface InvoiceOption {
    id: string;
    unitId: string;
    billingMonth: string;
    closingBalance: number;
    status: string;
}

interface Payment {
    id: string;
    invoiceId: string;
    amount: number;
    paymentDate: string;
    method: string;
}

export default function Payments() {
    const { tenantId } = useAuth();
    const [payments, setPayments] = useState<Payment[]>([]);
    const [invoices, setInvoices] = useState<InvoiceOption[]>([]);
    const [selectedInvoiceId, setSelectedInvoiceId] = useState("");
    const [amount, setAmount] = useState("");
    const [method, setMethod] = useState("UPI");

    useEffect(() => {
        apiClient.get(`/tenants/${tenantId}/payments`).then(res => setPayments(res.data));
        apiClient.get(`/tenants/${tenantId}/invoices`).then(res =>
            setInvoices(res.data.filter((i: InvoiceOption) => i.status !== "PAID"))
        );
    }, [tenantId]);

    async function handleCreate(e: React.FormEvent) {
        e.preventDefault();
        const invoice = invoices.find(i => i.id === selectedInvoiceId);
        if (!invoice) return;
        const res = await apiClient.post(`/tenants/${tenantId}/payments`, {
            invoiceId: invoice.id,
            unitId: invoice.unitId,
            amount: parseFloat(amount),
            method,
            paymentDate: new Date().toISOString().split("T")[0],
        });
        setPayments(prev => [...prev, res.data]);
        setInvoices(prev => prev.filter(i => i.id !== invoice.id));
        setSelectedInvoiceId(""); setAmount("");
    }

    return (
        <div>
            <div className="page-header">
                <h1>Payments</h1>
                <p>Money received, applied against invoices.</p>
            </div>

            <form className="ledger-form" onSubmit={handleCreate}>
                <select className="field" value={selectedInvoiceId} onChange={e => setSelectedInvoiceId(e.target.value)} required>
                    <option value="">Select invoice</option>
                    {invoices.map(i => (
                        <option key={i.id} value={i.id}>
                            {i.billingMonth} — ₹{i.closingBalance} due ({i.status})
                        </option>
                    ))}
                </select>
                <input className="field" placeholder="Amount" type="number" value={amount} onChange={e => setAmount(e.target.value)} required />
                <select className="field" value={method} onChange={e => setMethod(e.target.value)}>
                    <option>UPI</option><option>CASH</option><option>BANK_TRANSFER</option>
                </select>
                <button className="btn btn-primary" type="submit">Record payment</button>
            </form>

            {payments.length === 0 ? (
                <p className="ledger-empty">No payments recorded yet.</p>
            ) : (
                <table className="ledger-table">
                    <thead><tr><th>Invoice</th><th>Amount</th><th>Date</th><th>Method</th></tr></thead>
                    <tbody>
                    {payments.map(p => (
                        <tr key={p.id}>
                            <td>{p.invoiceId.replace("invoice::", "").slice(0, 8)}</td>
                            <td>₹{p.amount}</td><td>{p.paymentDate}</td><td>{p.method}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}