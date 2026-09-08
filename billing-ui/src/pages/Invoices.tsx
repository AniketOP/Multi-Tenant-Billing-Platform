import { useEffect, useState } from "react";
import apiClient from "../api/client";
import { useAuth } from "../context/AuthContext";
import AnimatedNumber from "../components/AnimatedNumber";
import StatusPill from "../components/StatusPill";

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

interface Unit {
    id: string;
    unitNumber: string;
}

export default function Invoices() {
    const { tenantId } = useAuth();
    const [invoices, setInvoices] = useState<Invoice[]>([]);
    const [units, setUnits] = useState<Unit[]>([]);
    const [genUnitId, setGenUnitId] = useState("");
    const [genMonth, setGenMonth] = useState("");
    const [error, setError] = useState("");
    const [genError, setGenError] = useState("");
    const [openUnit, setOpenUnit] = useState<string | null>(null);

    useEffect(() => {
        apiClient.get(`/tenants/${tenantId}/invoices`)
            .then(res => setInvoices(res.data))
            .catch(() => setError("Failed to load invoices"));
        apiClient.get(`/tenants/${tenantId}/units`).then(res => setUnits(res.data));
    }, [tenantId]);

    async function handleGenerate(e: React.FormEvent) {
        e.preventDefault();
        setGenError("");
        try {
            const res = await apiClient.post(
                `/tenants/${tenantId}/invoices/generate`,
                null,
                { params: { unitId: genUnitId, billingMonth: genMonth } }
            );
            setInvoices(prev => [...prev, res.data]);
            setOpenUnit(genUnitId);
            setGenUnitId(""); setGenMonth("");
        } catch (err: any) {
            setGenError(err?.response?.data?.message || "Could not generate — check the unit has a profile and the month isn't already billed.");
        }
    }

    const outstanding = invoices.filter(i => i.status !== "PAID").reduce((s, i) => s + i.closingBalance, 0);

    // Only show units that actually have at least one invoice, grouped
    const unitsWithInvoices = units.filter(u => invoices.some(i => i.unitId === u.id));

    return (
        <div>
            <div className="page-header">
                <h1>Invoices</h1>
                <p>Monthly bills, grouped by unit.</p>
            </div>

            <div className="stat-row">
                <div className="stat"><div className="stat-value"><AnimatedNumber value={outstanding} prefix="₹" /></div><div className="stat-label">Outstanding</div></div>
                <div className="stat"><div className="stat-value"><AnimatedNumber value={invoices.length} /></div><div className="stat-label">Total invoices</div></div>
            </div>

            <form className="ledger-form" onSubmit={handleGenerate}>
                <select className="field" value={genUnitId} onChange={e => setGenUnitId(e.target.value)} required>
                    <option value="">Select unit</option>
                    {units.map(u => <option key={u.id} value={u.id}>{u.unitNumber}</option>)}
                </select>
                <input className="field" type="month" value={genMonth} onChange={e => setGenMonth(e.target.value)} required />
                <button className="btn btn-primary" type="submit">Generate invoice</button>
            </form>
            {genError && <p className="error-text">{genError}</p>}
            {error && <p className="error-text">{error}</p>}

            {unitsWithInvoices.length === 0 && !error ? (
                <p className="ledger-empty">No invoices yet — generate one above.</p>
            ) : (
                unitsWithInvoices.map(unit => {
                    const unitInvoices = invoices
                        .filter(i => i.unitId === unit.id)
                        .sort((a, b) => b.billingMonth.localeCompare(a.billingMonth));
                    const unitDue = unitInvoices.filter(i => i.status !== "PAID").reduce((s, i) => s + i.closingBalance, 0);
                    const isOpen = openUnit === unit.id;

                    return (
                        <div key={unit.id} style={{ marginBottom: 4 }}>
                            <button
                                onClick={() => setOpenUnit(isOpen ? null : unit.id)}
                                style={{
                                    width: "100%", display: "flex", justifyContent: "space-between", alignItems: "center",
                                    background: "none", border: "none", borderBottom: "1px solid var(--rule-light)",
                                    padding: "14px 0", cursor: "pointer", textAlign: "left",
                                }}
                            >
                <span style={{ fontFamily: "'Fraunces', serif", fontSize: 17 }}>
                  {unit.unitNumber}
                    <span style={{ fontFamily: "'Inter', sans-serif", fontSize: 12, color: "var(--ink-text-dim)", marginLeft: 10 }}>
                    {unitInvoices.length} invoice{unitInvoices.length !== 1 ? "s" : ""}
                  </span>
                </span>
                                <span style={{ display: "flex", alignItems: "center", gap: 16 }}>
                  {unitDue > 0 && <span style={{ fontSize: 13, color: "var(--rust)" }}>₹{unitDue.toFixed(2)} due</span>}
                                    <span style={{ color: "var(--ink-text-dim)" }}>{isOpen ? "−" : "+"}</span>
                </span>
                            </button>

                            {isOpen && (
                                <table className="ledger-table" style={{ marginTop: 12, marginBottom: 12 }}>
                                    <thead>
                                    <tr><th>Month</th><th>Opening</th><th>Charges</th><th>Late fee</th><th>Closing</th><th>Status</th><th>Due</th></tr>
                                    </thead>
                                    <tbody>
                                    {unitInvoices.map(inv => (
                                        <tr key={inv.id}>
                                            <td>{inv.billingMonth}</td>
                                            <td>₹{inv.openingBalance}</td>
                                            <td>₹{inv.currentCharges}</td>
                                            <td>₹{inv.lateFee}</td>
                                            <td>₹{inv.closingBalance}</td>
                                            <td><StatusPill status={inv.status} /></td>
                                            <td>{inv.dueDate}</td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            )}
                        </div>
                    );
                })
            )}
        </div>
    );
}