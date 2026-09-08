import { useEffect, useState } from "react";
import apiClient from "../api/client";
import { useAuth } from "../context/AuthContext";

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
    const [invoiceId, setInvoiceId] = useState("");
    const [unitId, setUnitId] = useState("");
    const [amount, setAmount] = useState("");
    const [method, setMethod] = useState("UPI");

    async function fetchPayments() {
        const res = await apiClient.get(`/tenants/${tenantId}/payments`);
        setPayments(res.data);
    }

    useEffect(() => { fetchPayments(); }, [tenantId]);

    async function handleCreate(e: React.FormEvent) {
        e.preventDefault();
        await apiClient.post(`/tenants/${tenantId}/payments`, {
            invoiceId, unitId, amount: parseFloat(amount), method,
            paymentDate: new Date().toISOString().split("T")[0],
        });
        setInvoiceId(""); setUnitId(""); setAmount("");
        fetchPayments();
    }

    return (
        <div>
            <h2>Payments</h2>
            <form onSubmit={handleCreate} style={{ display: "flex", gap: 8, marginBottom: 16, flexWrap: "wrap" }}>
                <input placeholder="Invoice ID" value={invoiceId} onChange={e => setInvoiceId(e.target.value)} required />
                <input placeholder="Unit ID" value={unitId} onChange={e => setUnitId(e.target.value)} required />
                <input placeholder="Amount" type="number" value={amount} onChange={e => setAmount(e.target.value)} required />
                <select value={method} onChange={e => setMethod(e.target.value)}>
                    <option>UPI</option><option>CASH</option><option>BANK_TRANSFER</option>
                </select>
                <button type="submit">Record Payment</button>
            </form>
            <table style={{ width: "100%", borderCollapse: "collapse" }}>
                <thead><tr><th>Invoice</th><th>Amount</th><th>Date</th><th>Method</th></tr></thead>
                <tbody>
                {payments.map(p => (
                    <tr key={p.id}>
                        <td>{p.invoiceId.replace("invoice::", "").slice(0, 8)}</td>
                        <td>{p.amount}</td><td>{p.paymentDate}</td><td>{p.method}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}