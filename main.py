from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.label import Label
from kivy.uix.button import Button


class MainApp(App):
    def build(self):
        layout = BoxLayout(
            orientation="vertical",
            padding=30,
            spacing=20
        )

        label = Label(
            text="Halo dari Android!",
            font_size=32
        )

        button = Button(
            text="Klik Saya",
            font_size=24
        )

        button.bind(on_press=self.tombol_diklik)

        layout.add_widget(label)
        layout.add_widget(button)

        return layout

    def tombol_diklik(self, instance):
        instance.text = "Berhasil diklik!"


if __name__ == "__main__":
    MainApp().run()
