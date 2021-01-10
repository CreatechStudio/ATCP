from process import *
from client import *


class MyProcess(Process):

    def _on_sent(self, mid: ID.MID):
        pass

    def _on_recved(self, mid: ID.MID, result: str):
        pass

    def _process(self, msg: str) -> str:
        pass


class SampleClient(Client):
    def __init__(self):
        super().__init__()
        values.put('name', 'sample')
        values.put('project', 'sample')
        values.put('key_length', 2048)
        self.set_process(MyProcess())
