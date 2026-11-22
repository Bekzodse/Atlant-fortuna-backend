"use client";

import PhoneInputWithCountry, { type Value } from "react-phone-number-input";
import { cn } from "@/lib/utils";
import "react-phone-number-input/style.css";

interface PhoneInputProps {
  id?: string;
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
  error?: string;
  className?: string;
}

export function PhoneInput({ id, value, onChange, required, error, className }: PhoneInputProps) {
  return (
    <div className="space-y-1">
      <PhoneInputWithCountry
        id={id}
        international
        limitMaxLength
        defaultCountry="UZ"
        value={value as Value}
        onChange={(v) => onChange(v ?? "")}
        required={required}
        className={cn(
          "phone-input-field flex h-9 w-full min-w-0 rounded-md border border-input bg-transparent px-3 py-1 text-base shadow-xs transition-[color,box-shadow] md:text-sm",
          "focus-within:border-ring focus-within:ring-[3px] focus-within:ring-ring/50",
          error && "border-destructive ring-destructive/20",
          className
        )}
      />
      {error && <p className="text-sm text-destructive">{error}</p>}
    </div>
  );
}
