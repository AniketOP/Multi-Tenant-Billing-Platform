import { useEffect, useRef, useState } from "react";

export default function AnimatedNumber({ value, prefix = "" }: { value: number; prefix?: string }) {
    const [display, setDisplay] = useState(0);
    const prevValue = useRef(0);

    useEffect(() => {
        const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
        if (reduceMotion) {
            setDisplay(value);
            return;
        }
        const start = prevValue.current;
        const change = value - start;
        const duration = 500;
        const startTime = performance.now();

        function tick(now: number) {
            const progress = Math.min((now - startTime) / duration, 1);
            const eased = 1 - Math.pow(1 - progress, 3);
            setDisplay(start + change * eased);
            if (progress < 1) requestAnimationFrame(tick);
            else prevValue.current = value;
        }
        requestAnimationFrame(tick);
    }, [value]);

    return <>{prefix}{display.toLocaleString(undefined, { maximumFractionDigits: 2 })}</>;
}