import { useEffect, useState } from "react";
import apiClient from "../api/client";
import { useAuth } from "../context/AuthContext";
import AnimatedNumber from "../components/AnimatedNumber";

interface Unit {
    id: string;
    unitNumber: string;
    active: boolean;
    profileId: string | null;
}

interface Profile {
    id: string;
    name: string;
}

export default function Dashboard() {
    const { tenantId } = useAuth();
    const [units, setUnits] = useState<Unit[]>([]);
    const [profiles, setProfiles] = useState<Profile[]>([]);
    const [unitNumber, setUnitNumber] = useState("");
    const [error, setError] = useState("");

    useEffect(() => {
        apiClient.get(`/tenants/${tenantId}/units`).then(res => setUnits(res.data)).catch(() => setError("Failed to load units"));
        apiClient.get(`/tenants/${tenantId}/profiles`).then(res => setProfiles(res.data));
    }, [tenantId]);

    async function handleCreate(e: React.FormEvent) {
        e.preventDefault();
        const res = await apiClient.post(`/tenants/${tenantId}/units`, { unitNumber, active: true });
        setUnits(prev => [...prev, res.data]);
        setUnitNumber("");
    }

    async function assignProfile(unit: Unit, profileId: string) {
        const res = await apiClient.patch(`/tenants/${tenantId}/units/${unit.id}`, {
            unitNumber: unit.unitNumber, active: unit.active, profileId,
        });
        setUnits(prev => prev.map(u => u.id === unit.id ? res.data : u));
    }

    const activeCount = units.filter(u => u.active).length;

    return (
        <div>
            <div className="page-header">
                <h1>Units</h1>
                <p>Every apartment registered to this society.</p>
            </div>

            <div className="stat-row">
                <div className="stat"><div className="stat-value"><AnimatedNumber value={units.length} /></div><div className="stat-label">Total units</div></div>
                <div className="stat"><div className="stat-value"><AnimatedNumber value={activeCount} /></div><div className="stat-label">Active</div></div>
            </div>

            <form className="ledger-form" onSubmit={handleCreate}>
                <input className="field" placeholder="Unit number, e.g. A-101" value={unitNumber} onChange={e => setUnitNumber(e.target.value)} required />
                <button className="btn btn-primary" type="submit">Add unit</button>
            </form>

            {error && <p className="error-text">{error}</p>}
            {units.length === 0 && !error ? (
                <p className="ledger-empty">No units yet — add the first one above.</p>
            ) : (
                <table className="ledger-table">
                    <thead><tr><th>Unit</th><th>Status</th><th>Billing profile</th></tr></thead>
                    <tbody>
                    {units.map(u => (
                        <tr key={u.id}>
                            <td>{u.unitNumber}</td>
                            <td>{u.active ? "Active" : "Inactive"}</td>
                            <td>
                                <select className="field" value={u.profileId ?? ""} onChange={e => assignProfile(u, e.target.value)}>
                                    <option value="">Unassigned</option>
                                    {profiles.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                                </select>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}