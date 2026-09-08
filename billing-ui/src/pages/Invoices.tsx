import { useEffect, useState } from "react";
import apiClient from "../api/client";
import { useAuth } from "../context/AuthContext";

interface Invoice {
    id: string;
    unitId: string;
    billingMonth: string;
    openingBalance: number;
    currentCharges: number;
    lateFee: number;
    closingBalance: number;
    status: string;
    dueDate: string;
}

export default function Invoices() {
    const { tenantId } = useAuth();
    const [invoices, setInvoices] = useState<Invoice[]>([]);

    async function fetchInvoices() {
        const res = await apiClient.get(`/tenants/${tenantId}/invoices`);
        setInvoices(res.data);
    }

    useEffect(() => { fetchInvoices(); }, [tenantId]);

    return (
        <div>
            <h2>Invoices</h2>
            <table style={{ width: "100%", borderCollapse: "collapse" }}>
                <thead>
                <tr>
                    <th>Month</th><th>Unit</th><th>Opening</th><th>Charges</th>
                    <th>Late Fee</th><th>Closing</th><th>Status</th><th>Due</th>
                </tr>
                </thead>
                <tbody>
                {invoices.map(inv => (
                    <tr key={inv.id}>
                        <td>{inv.billingMonth}</td>
                        <td>{inv.unitId.replace("unit::", "").slice(0, 8)}</td>
                        <td>{inv.openingBalance}</td>
                        <td>{inv.currentCharges}</td>
                        <td>{inv.lateFee}</td>
                        <td>{inv.closingBalance}</td>
                        <td>{inv.status}</td>
                        <td>{inv.dueDate}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}