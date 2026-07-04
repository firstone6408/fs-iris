export default async function HomePage() {
  let message = "Welcome to Iris";
  try {
    const res = await fetch("http://localhost:8000/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        max_token: 2048,
        messages: [{ role: "user", content: "สวัสดีแนะนำตัวเองสั้นๆ" }],
      }),
    });

    const data = await res.json();
    message = data.reply;
  } catch (error) {
    console.error(error);
  }

  return (
    <div className="h-screen flex flex-col justify-center items-center">
      <div className="text-2xl space-x-1">
        <span>Hello</span>
        <span className="font-bold text-blue-600">Iris AI</span>
      </div>
      <div className="w-[50%] text-center text-gray-500">{message}</div>
    </div>
  );
}
