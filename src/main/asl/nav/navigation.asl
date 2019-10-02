// Agent navigation in project massim2019
{ include("common.asl") }
{ include("internal_actions.asl") }

{ begin namespace(nav, global) }

{ include("actions/actions.asl") }

// Include Navigation sub-modules
{ include("nav/nav_common.asl") }
{ include("nav/better_navigation.asl") }
{ include("nav/block_nav.asl") }
{ include("nav/explore.asl") }
{ include("nav/obtain_block.asl") }
{ include("nav/navigate_to_goal.asl") }

{ end } /* End Navigation name space */