"use client";

import { useState } from "react";
import { Plus, Trash2, Send, KeyRound, CheckCircle2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { telegramApi } from "@/lib/api";
import { useTelegramBots, useTelegramChats } from "@/hooks/use-api";
import { toast } from "sonner";
import { AxiosError } from "axios";

export default function TelegramPage() {
  const { bots, isLoading: botsLoading, mutate: mutateBots } = useTelegramBots();
  const { chats, isLoading: chatsLoading, mutate: mutateChats } = useTelegramChats();

  const [isAddBotOpen, setIsAddBotOpen] = useState(false);
  const [newBot, setNewBot] = useState({ label: "", token: "" });
  const [activatingBotId, setActivatingBotId] = useState<string | null>(null);
  const [deleteBotId, setDeleteBotId] = useState<string | null>(null);

  const [isAddChatOpen, setIsAddChatOpen] = useState(false);
  const [newChat, setNewChat] = useState({ label: "", groupId: "" });
  const [deleteChatId, setDeleteChatId] = useState<string | null>(null);
  const [testingId, setTestingId] = useState<string | null>(null);

  const handleAddBot = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await telegramApi.createBot(newBot);
      toast.success("Bot added");
      setIsAddBotOpen(false);
      setNewBot({ label: "", token: "" });
      mutateBots();
    } catch (error) {
      const axiosError = error as AxiosError<{ message?: string }>;
      toast.error(axiosError.response?.data?.message || "Failed to add bot");
    }
  };

  const handleActivateBot = async (id: string) => {
    setActivatingBotId(id);
    try {
      await telegramApi.activateBot(id);
      mutateBots();
    } catch (error) {
      toast.error("Failed to switch active bot");
    } finally {
      setActivatingBotId(null);
    }
  };

  const handleDeleteBot = async () => {
    if (!deleteBotId) return;
    try {
      await telegramApi.deleteBot(deleteBotId);
      toast.success("Bot deleted");
      mutateBots();
    } catch (error) {
      toast.error("Failed to delete bot");
    } finally {
      setDeleteBotId(null);
    }
  };

  const handleAddChat = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await telegramApi.createChat(newChat);
      toast.success("Chat added");
      setIsAddChatOpen(false);
      setNewChat({ label: "", groupId: "" });
      mutateChats();
    } catch (error) {
      const axiosError = error as AxiosError<{ message?: string }>;
      toast.error(axiosError.response?.data?.message || "Failed to add chat");
    }
  };

  const handleToggleChatActive = async (id: string, active: boolean) => {
    mutateChats(
      chats?.map((c) => (c.id === id ? { ...c, active } : c)),
      false
    );
    try {
      await telegramApi.updateChat(id, { active });
    } catch (error) {
      toast.error("Failed to update chat");
      mutateChats();
    }
  };

  const handleDeleteChat = async () => {
    if (!deleteChatId) return;
    try {
      mutateChats(chats?.filter((c) => c.id !== deleteChatId), false);
      await telegramApi.deleteChat(deleteChatId);
      toast.success("Chat deleted");
    } catch (error) {
      toast.error("Failed to delete chat");
      mutateChats();
    } finally {
      setDeleteChatId(null);
    }
  };

  const handleTest = async (id: string) => {
    setTestingId(id);
    try {
      await telegramApi.testChat(id);
      toast.success("Test message sent");
    } catch (error) {
      const axiosError = error as AxiosError<{ message?: string }>;
      toast.error(axiosError.response?.data?.message || "Failed to send test message");
    } finally {
      setTestingId(null);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Telegram</h1>
        <p className="text-muted-foreground">Bots and group chats for contact-form notifications</p>
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <div>
            <CardTitle className="flex items-center gap-2"><KeyRound className="h-4 w-4" />Bots</CardTitle>
            <CardDescription>Add as many bot tokens as you need; pick which one is active</CardDescription>
          </div>
          <Dialog open={isAddBotOpen} onOpenChange={setIsAddBotOpen}>
            <DialogTrigger asChild><Button><Plus className="h-4 w-4 mr-2" />Add Bot</Button></DialogTrigger>
            <DialogContent>
              <DialogHeader><DialogTitle>Add Bot</DialogTitle></DialogHeader>
              <form onSubmit={handleAddBot} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="bot-label">Label</Label>
                  <Input
                    id="bot-label"
                    placeholder="e.g. Production bot"
                    value={newBot.label}
                    onChange={(e) => setNewBot({ ...newBot, label: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="bot-token">Bot Token</Label>
                  <Input
                    id="bot-token"
                    type="password"
                    placeholder="123456:ABC-DEF..."
                    value={newBot.token}
                    onChange={(e) => setNewBot({ ...newBot, token: e.target.value })}
                    required
                  />
                </div>
                <Button type="submit">Add Bot</Button>
              </form>
            </DialogContent>
          </Dialog>
        </CardHeader>
        <CardContent>
          {botsLoading ? (
            <div>Loading...</div>
          ) : !bots || bots.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-muted-foreground">
              <KeyRound className="h-12 w-12 mb-4" />
              <p>No bots configured</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Label</TableHead>
                  <TableHead>Token</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {bots.map((bot) => (
                  <TableRow key={bot.id}>
                    <TableCell className="font-medium">{bot.label}</TableCell>
                    <TableCell className="font-mono text-sm">{bot.maskedToken}</TableCell>
                    <TableCell>
                      {bot.active ? (
                        <Badge className="gap-1"><CheckCircle2 className="h-3 w-3" />Active</Badge>
                      ) : (
                        <Badge variant="outline">Inactive</Badge>
                      )}
                    </TableCell>
                    <TableCell className="text-right space-x-2">
                      {!bot.active && (
                        <Button
                          variant="outline"
                          size="sm"
                          disabled={activatingBotId === bot.id}
                          onClick={() => handleActivateBot(bot.id)}
                        >
                          {activatingBotId === bot.id ? "Switching..." : "Set Active"}
                        </Button>
                      )}
                      <Button variant="outline" size="sm" onClick={() => setDeleteBotId(bot.id)}>
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <div>
            <CardTitle>Chat Groups</CardTitle>
            <CardDescription>Every active chat receives contact-form notifications</CardDescription>
          </div>
          <Dialog open={isAddChatOpen} onOpenChange={setIsAddChatOpen}>
            <DialogTrigger asChild><Button><Plus className="h-4 w-4 mr-2" />Add Chat</Button></DialogTrigger>
            <DialogContent>
              <DialogHeader><DialogTitle>Add Chat Group</DialogTitle></DialogHeader>
              <form onSubmit={handleAddChat} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="label">Label</Label>
                  <Input
                    id="label"
                    placeholder="e.g. Sales team"
                    value={newChat.label}
                    onChange={(e) => setNewChat({ ...newChat, label: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="groupId">Group ID</Label>
                  <Input
                    id="groupId"
                    placeholder="-1001234567890"
                    value={newChat.groupId}
                    onChange={(e) => setNewChat({ ...newChat, groupId: e.target.value })}
                    required
                  />
                </div>
                <Button type="submit">Add Chat</Button>
              </form>
            </DialogContent>
          </Dialog>
        </CardHeader>
        <CardContent>
          {chatsLoading ? (
            <div>Loading...</div>
          ) : !chats || chats.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-muted-foreground">
              <Send className="h-12 w-12 mb-4" />
              <p>No chat groups configured</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Label</TableHead>
                  <TableHead>Group ID</TableHead>
                  <TableHead>Active</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {chats.map((chat) => (
                  <TableRow key={chat.id}>
                    <TableCell className="font-medium">{chat.label}</TableCell>
                    <TableCell className="font-mono text-sm">{chat.groupId}</TableCell>
                    <TableCell>
                      <Switch
                        checked={chat.active}
                        onCheckedChange={(checked) => handleToggleChatActive(chat.id, checked)}
                      />
                    </TableCell>
                    <TableCell className="text-right space-x-2">
                      <Button
                        variant="outline"
                        size="sm"
                        disabled={testingId === chat.id}
                        onClick={() => handleTest(chat.id)}
                      >
                        <Send className="h-4 w-4 mr-2" />
                        {testingId === chat.id ? "Sending..." : "Test"}
                      </Button>
                      <Button variant="outline" size="sm" onClick={() => setDeleteChatId(chat.id)}>
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <AlertDialog open={!!deleteBotId} onOpenChange={() => setDeleteBotId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you sure?</AlertDialogTitle>
            <AlertDialogDescription>This will permanently remove this bot. If it was active, the next most recent bot takes over automatically.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleDeleteBot}>Delete</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <AlertDialog open={!!deleteChatId} onOpenChange={() => setDeleteChatId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you sure?</AlertDialogTitle>
            <AlertDialogDescription>This will permanently remove this chat group.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleDeleteChat}>Delete</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
