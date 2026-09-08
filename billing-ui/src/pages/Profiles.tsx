import { useEffect, useState } from "react";
import apiClient from "../api/client";
import { useAuth } from "../context/AuthContext";

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
        await apiClient.post(`/tenants/${tenantId}/profiles`, { name, baseCharge: parseFloat(baseCharge) });
        setName(""); setBaseCharge("");
        fetchProfiles();
    }

    return (
        <div>
            <h2>Profiles</h2>
            <form onSubmit={handleCreate} style={{ display: "flex", gap: 8, marginBottom: 16 }}>
                <input placeholder="Name" value={name} onChange={e => setName(e.target.value)} required />
                <input placeholder="Base Charge" type="number" value={baseCharge} onChange={e => setBaseCharge(e.target.value)} required />
                <button type="submit">Add Profile</button>
            </form>
            <table style={{ width: "100%", borderCollapse: "collapse" }}>
                <thead><tr><th>Name</th><th>Base Charge</th></tr></thead>
                <tbody>
                {profiles.map(p => (
                    <tr key={p.id}><td>{p.name}</td><td>{p.baseCharge}</td></tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}