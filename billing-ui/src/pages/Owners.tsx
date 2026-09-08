import { useEffect, useState } from "react";
import apiClient from "../api/client";
import { useAuth } from "../context/AuthContext";

interface Owner {
    id: string;
    name: string;
    email: string;
    phone: string;
    unitIds: string[];
}

interface Unit {
    id: string;
    unitNumber: string;
}

export default function Owners() {
    const { tenantId } = useAuth();
    const [owners, setOwners] = useState<Owner[]>([]);
    const [units, setUnits] = useState<Unit[]>([]);
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");

    useEffect(() => {
        apiClient.get(`/tenants/${tenantId}/owners`).then(res => setOwners(res.data));
        apiClient.get(`/tenants/${tenantId}/units`).then(res => setUnits(res.data));
    }, [tenantId]);

    async function handleCreate(e: React.FormEvent) {
        e.preventDefault();
        const res = await apiClient.post(`/tenants/${tenantId}/owners`, { name, email, phone, unitIds: [] });
        setOwners(prev => [...prev, res.data]);
        setName(""); setEmail(""); setPhone("");
    }

    function ownerOf(unitId: string) {
        return owners.find(o => o.unitIds.includes(unitId));
    }

    async function linkUnit(owner: Owner, unitId: string) {
        if (!unitId || owner.unitIds.includes(unitId)) return;
        const claimedBy = ownerOf(unitId);
        if (claimedBy) return; // guarded in UI, dropdown already excludes these
        const newUnitIds = [...owner.unitIds, unitId];
        const res = await apiClient.patch(`/tenants/${tenantId}/owners/${owner.id}`, {
            name: owner.name, email: owner.email, phone: owner.phone, unitIds: newUnitIds,
        });
        setOwners(prev => prev.map(o => o.id === owner.id ? res.data : o));
    }

    async function unlinkUnit(owner: Owner, unitId: string) {
        const newUnitIds = owner.unitIds.filter(id => id !== unitId);
        const res = await apiClient.patch(`/tenants/${tenantId}/owners/${owner.id}`, {
            name: owner.name, email: owner.email, phone: owner.phone, unitIds: newUnitIds,
        });
        setOwners(prev => prev.map(o => o.id === owner.id ? res.data : o));
    }

    function unitLabel(id: string) {
        return units.find(u => u.id === id)?.unitNumber ?? id.slice(0, 8);
    }

    return (
        <div>
            <div className="page-header">
                <h1>Owners</h1>
                <p>Everyone who holds a unit in this society.</p>
            </div>

            <form className="ledger-form" onSubmit={handleCreate}>
                <input className="field" placeholder="Name" value={name} onChange={e => setName(e.target.value)} required />
                <input className="field" placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} required />
                <input className="field" placeholder="Phone" value={phone} onChange={e => setPhone(e.target.value)} required />
                <button className="btn btn-primary" type="submit">Add owner</button>
            </form>

            {owners.length === 0 ? (
                <p className="ledger-empty">No owners yet — add the first one above.</p>
            ) : (
                <table className="ledger-table">
                    <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Units held</th><th>Link a unit</th></tr></thead>
                    <tbody>
                    {owners.map(o => (
                        <tr key={o.id}>
                            <td>{o.name}</td><td>{o.email}</td><td>{o.phone}</td>
                            <td>
                                {o.unitIds?.length
                                    ? o.unitIds.map(id => (
                                        <span key={id} style={{ marginRight: 8 }}>
                          {unitLabel(id)}
                                            <button
                                                onClick={() => unlinkUnit(o, id)}
                                                style={{ marginLeft: 4, border: "none", background: "none", color: "var(--rust)", cursor: "pointer", fontSize: 12 }}
                                                title="Unlink"
                                            >
                            ✕
                          </button>
                        </span>
                                    ))
                                    : "—"}
                            </td>
                            <td>
                                <select className="field" value="" onChange={e => linkUnit(o, e.target.value)}>
                                    <option value="">Select unit</option>
                                    {units.filter(u => !ownerOf(u.id)).map(u => (
                                        <option key={u.id} value={u.id}>{u.unitNumber}</option>
                                    ))}
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