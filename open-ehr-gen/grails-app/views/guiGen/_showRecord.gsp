<%@ page import="archetype_repository.ArchetypeManager" %>

    <div>
      <table cellpadding="0" cellspacing="3">
        <tr>
          <td>
            <g:each in="${composition.content}" var="content">
              <g:set var="archetype" value="${ArchetypeManager.getInstance().getArchetype( content.archetypeDetails.archetypeId )}" />
              <g:render template="../guiGen/showTemplates/Locatable"
                        model="[rmNode: content, archetype: archetype ]" />
            </g:each>
          </td>
        </tr>
      </table>
    </div>
