from client import *
from process import Process
from packages.data.values import *
from packages.data.id import *
from packages import basics


class MyActionCallback(Process.ActionCallback):

    def on_send(self):
        pass

    def on_sent(self):
        pass

    def on_recved(self, result: str):
        print('M')
        print(result)


class MyProcess(Process):
    def _on_killed(self):
        self.close()

    def _on_recved(self, mid: ID.MID, result: str):
        print(result)
        if result == 'recved msg: hello again':
            self.kill()
            pass

    def _process(self, msg: str) -> str:
        print(msg)
        return 'recved: ' + msg

    def _on_sent(self, mid: ID.MID):
        pass


c = Client(MyProcess('127.0.0.1', 2000))
c.start()
c.send('hello', Process.FLAG_RESULT_NO_NEED)
print('A')
c.send('hello again', Process.FLAG_RESULT_NO_NEED)
print('B')
c.send('hello again')
print('C')
