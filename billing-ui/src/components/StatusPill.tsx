export default function StatusPill({ status }: { status: string }) {
    const cls =
        status === "PAID" ? "status-paid" :
            status === "PARTIAL" ? "status-partial" :
                status === "OVERDUE" ? "status-overdue" :
                    "status-pending";
    return <span className={`status-pill ${cls}`}>{status}</span>;
}