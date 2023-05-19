package me.lanzhi.api

interface ConnectionHandler
{
    fun onChannelCreated(channel: Channel, channelReason: ChannelReason) = Unit
    fun onChannelClosed(channel: Channel, channelReason: ChannelReason) = Unit
}