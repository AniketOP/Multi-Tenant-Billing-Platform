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

export default function Owners() {
    const { tenantId } = useAuth();
    const [owners, setOwners] = useState<Owner[]>([]);
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");

    async function fetchOwners() {
        const res = await apiClient.get(`/tenants/${tenantId}/owners`);
        setOwners(res.data);
    }

    useEffect(() => { fetchOwners(); }, [tenantId]);

    async function handleCreate(e: React.FormEvent) {
        e.preventDefault();
        await apiClient.post(`/tenants/${tenantId}/owners`, { name, email, phone, unitIds: [] });
        setName(""); setEmail(""); setPhone("");
        fetchOwners();
    }

    return (
        <div>
            <h2>Owners</h2>
            <form onSubmit={handleCreate} style={{ display: "flex", gap: 8, marginBottom: 16 }}>
                <input placeholder="Name" value={name} onChange={e => setName(e.target.value)} required />
                <input placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} required />
                <input placeholder="Phone" value={phone} onChange={e => setPhone(e.target.value)} required />
                <button type="submit">Add Owner</button>
            </form>
            <table style={{ width: "100%", borderCollapse: "collapse" }}>
                <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Units</th></tr></thead>
                <tbody>
                {owners.map(o => (
                    <tr key={o.id}>
                        <td>{o.name}</td><td>{o.email}</td><td>{o.phone}</td><td>{o.unitIds?.length ?? 0}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}