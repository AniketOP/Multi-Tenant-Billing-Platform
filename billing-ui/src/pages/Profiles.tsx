import { useEffect, useState } from "react";
import apiClient from "../api/client";
import { useAuth } from "../context/AuthContext";
import AnimatedNumber from "../components/AnimatedNumber";

interface Profile {
    id: string;
    name: string;
    baseCharge: number;
}

export default function Profiles() {
    const { tenantId } = useAuth();
    const [profiles, setProfiles] = useState<Profile[]>([]);
    const [name, setName] = useState("");
    const [baseCharge, setBaseCharge] = useState("");

    async function fetchProfiles() {
        const res = await apiClient.get(`/tenants/${tenantId}/profiles`);
        setProfiles(res.data);
    }

    useEffect(() => { fetchProfiles(); }, [tenantId]);

    async function handleCreate(e: React.FormEvent) {
        e.preventDefault();
        const res = await apiClient.post(`/tenants/${tenantId}/profiles`, { name, baseCharge: parseFloat(baseCharge) });
        setProfiles(prev => [...prev, res.data]);
        setName(""); setBaseCharge("");
    }

    return (
        <div>
            <div className="page-header">
                <h1>Billing profiles</h1>
                <p>Rate plans units are linked to.</p>
            </div>

            <form className="ledger-form" onSubmit={handleCreate}>
                <input className="field" placeholder="Name, e.g. Standard 2BHK" value={name} onChange={e => setName(e.target.value)} required />
                <input className="field" placeholder="Base charge" type="number" value={baseCharge} onChange={e => setBaseCharge(e.target.value)} required />
                <button className="btn btn-primary" type="submit">Add profile</button>
            </form>

            {profiles.length === 0 ? (
                <p className="ledger-empty">No profiles yet — add the first one above.</p>
            ) : (
                <table className="ledger-table">
                    <thead><tr><th>Name</th><th>Base charge</th></tr></thead>
                    <tbody>
                    {profiles.map(p => (
                        <tr key={p.id}>
                            <td>{p.name}</td>
                            <td><AnimatedNumber value={p.baseCharge} prefix="₹" /></td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}