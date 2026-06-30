"""Interactive command-line interface for chatting with Iris."""

from application.chat_service import ChatService


class CLI:
    """
    Text-based REPL (Read-Eval-Print Loop) for the Iris assistant.

    Reads one line of user input at a time, passes it to ChatService,
    and prints the model's reply. Skips blank input. Exits cleanly when
    the user types 'exit'.
    """

    def __init__(self, chat_service: ChatService) -> None:
        """
        Args:
            chat_service: The ChatService instance that handles conversation logic.
        """
        self._chat_service = chat_service

    def run(self) -> None:
        """
        Start the interactive chat loop and block until the user exits.

        Behavior:
        - Prints a ready message on startup.
        - Skips empty input without sending it to the model.
        - Sends user input to ChatService and prints the reply.
        - Stops when the user types 'exit' (case-insensitive).
        """
        print("Ready! (type 'exit' to quit)\n")

        while True:
            user_input = input("You: ").strip()

            if not user_input:
                continue

            if user_input.lower() == "exit":
                break

            reply = self._chat_service.chat(user_input)
            print(f"Iris: {reply}\n")
