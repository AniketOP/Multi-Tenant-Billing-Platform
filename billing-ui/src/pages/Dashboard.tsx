import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../api/client";
import { useAuth } from "../context/AuthContext";

interface Unit {
    id: string;
    unitNumber: string;
    active: boolean;
    profileId: string | null;
}

export default function Dashboard() {
    const { tenantId, username, logout } = useAuth();
    const navigate = useNavigate();
    const [units, setUnits] = useState<Unit[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        async function fetchUnits() {
            try {
                const response = await apiClient.get(`/tenants/${tenantId}/units`);
                setUnits(response.data);
            } catch (err) {
                setError("Failed to load units");
            } finally {
                setLoading(false);
            }
        }
        fetchUnits();
    }, [tenantId]);

    function handleLogout() {
        logout();
        navigate("/login");
    }

    if (loading) return <p>Loading...</p>;

    return (
        <div style={{ maxWidth: 800, margin: "40px auto" }}>
            <div style={{ display: "flex", justifyContent: "space-between" }}>
                <h2>Welcome, {username}</h2>
                <button onClick={handleLogout}>Log Out</button>
            </div>

            <h3>Units</h3>
            {error && <p style={{ color: "red" }}>{error}</p>}
            {units.length === 0 && !error && <p>No units yet.</p>}

            <table style={{ width: "100%", borderCollapse: "collapse" }}>
                <thead>
                <tr>
                    <th style={{ textAlign: "left" }}>Unit Number</th>
                    <th style={{ textAlign: "left" }}>Active</th>
                </tr>
                </thead>
                <tbody>
                {units.map((unit) => (
                    <tr key={unit.id}>
                        <td>{unit.unitNumber}</td>
                        <td>{unit.active ? "Yes" : "No"}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}