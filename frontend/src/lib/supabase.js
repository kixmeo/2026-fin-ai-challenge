import { createClient } from "@supabase/supabase-js";

const url = import.meta.env.VITE_SUPABASE_URL;
const anonKey = import.meta.env.VITE_SUPABASE_ANON_KEY;

// .env에 Supabase 값이 아직 없으면 null이 되고, 앱은 자동으로 mock 로그인으로 동작해요.
export const supabase = url && anonKey ? createClient(url, anonKey) : null;

export async function getAccessToken() {
  if (!supabase) return null;
  const { data } = await supabase.auth.getSession();
  return data?.session?.access_token ?? null;
}
